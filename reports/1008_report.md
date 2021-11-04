# Requirements
- 주어진 SNP를 기반으로 itrc_snp.. file 에서 input으로 들어가는 testcase 별로 geno 0, geno 1 그리고 geno 2의 결과를 출력
- PC 통신을 할 수 있는 환경에서 앱을 실행

우선 주어진 데이터를 기반으로 애플리케이션과 서버를 어떻게 연동할 지 구상을 해보았다.

1) 결과를 알고 싶은 테스트 데이터를 입력
2) 주어진 Reference 내에서 결과를 취합
3) 요청한 사용자에게 전송

위와 같이 구현하기 위해서 우선 csv file을 통해서 count, max P value 그리고 min P value를 각 geno 마다 알아내야 하기 때문에 Reference를 탐색에 용이하게 변환해줄 필요가 있다.

Reference를 list로 만들어서 비교를 하기에는 많은 시간이 걸릴 거 같아서 indexing 작업을 최대한 최소화 하고자 했다. 그래서 Reference를 Hashmap으로 저장해 indexing 시간을 O(1)으로 하였고 value 값으로는 갖고 있는 P.VAL 값을 저장해두었다. (이 내용은 추후에 다른 column의 내용이 필요하다면 바뀔 수 있는 부분이다.)

통신에 앞서 예측할 수 있는 알고리즘을 구현하였다. Hashmap안에 테스트하는 SNP값이 key값으로 존재하는 경우만 비교를 해서 예외처리를 해주었고, 주어진 P.VAL 값을 비교하는 데 최소한의 시간으로 비교를 해주었다.

결과적으로 하나의 테스트 케이스를 비교하는데 걸리는 시간은 총 테스트 케이스의 row의 수, 즉 테스트 케이스의 갯수만큼 **O(n)**의 시간이 걸리게 된다.

# Server
앞서 작업한 예측결과를 반환하는 함수를 Python으로 작성해서 서버 또한 Python을 이용하기로 하였다. Flask를 이용해서 간단한 RESTful API를 구현해보았다.

```
app
├── resource
│   └── api.py
├── service
│   └── prediction_service.py
└── app.py
```

아직은 데이터를 관리하거나 하는 작업이 없어서 별도의 데이터베이스를 구축하지 않았다.
- api

    사용자가 서버에 접근하는 End-point를 관리한다. class로 구현해 GET, POST, PUT, DELETE등 다양한 method를 구현할 수 있다.

- service

    현재는 입력받은 테스트 케이스의 결과값을 반환해주는 작업을 해주고 있다.

- dataset

    주어진 데이터를 저장해둔 곳이다.

## API
Hypertensions 라는 class를 구현하였는데 코드는 아래와 같다.

``` python
from flask import current_app, g
from flask_pymongo import PyMongo
from flask_restful import Resource, reqparse
import werkzeug

from service.prediction_service import HypertensionService


class Hypertensions(Resource):
    """methods with Hypertensions

    Methods
    -------
    get()
        returns the result of geno 0, geno 1 and geno 2 evaluted
        which is based on itrc_snp_hypertension_sm
    """
    def __init__(self):
        db = g._database = PyMongo(current_app).db
        self.service = HypertensionService(db)


    def post(self):
        try:
            # create parser to parse request
            parser = reqparse.RequestParser()
            parser.add_argument('file',
                                type=werkzeug.datastructures.FileStorage,
                                location='files')
            args = parser.parse_args()

            file = args.get('file')

            g0, g1, g2 = self.service.evaluate_testcase(file)

            # make it into format
            res = {}
            res['result'] = []
            return_list = [g0, g1, g2]

            for index in range(3):
                name_dict = {'name': f'geno{(index + 1)}'}
                name_dict.update(return_list[index])
                res['result'].append(name_dict)

            return res, 200

        except Exception as e:
            return {'result': str(e)}, 400
```

직접적으로 Api layer에서 연산을 다루지 않고 service layer로의 호출만이 이루어진다. 아직은 따로 함수들을 나누지는 않았지만 추후 업데이트 예정이다.

`Hypertensions` 클래스 내 method는 현재 하나로 이루어져있다. 사용자로부터 입력받을 file을 parsing해서 관련 데이터 값으 service layer로 넘겨준다.

### POST 
사용자로부터 POST 요청이 오게되면 'file'이란 `key`값을 가지고 있는 multipart/form-data를 확인한다. 그리고 관련 작업은 service 레이어에서 이루어진다. 만약에 `file` 이라는 data가 첨부되지 않았다면 status code 400과 함께 에러를 반환한다.


#### Result
위와 같이 작성한 클래스로 요청을 아래와 같이 보낼 수 있다.

``` bash
localhost:3000/hypertensions
```

이와 같이 보냈을 경우, 아래와 같은 결과 값을 서버로 부터 얻을 수 있다.

``` json
{
    "result": {
        "geno0": {
            "cnt": 10576,
            "max_p": 5e-08,
            "min_p": 5.7e-109
        },
        "geno1": {
            "cnt": 3928,
            "max_p": 5e-08,
            "min_p": 3.3e-108
        },
        "geno2": {
            "cnt": 1221,
            "max_p": 4.9e-08,
            "min_p": 7.5e-64
        }
    }
}
```

# Client
Client는 Android로 구성하였다. 요구사항에 맞게 사용자는 원하는 파일을 3개 중에 하나를 선택할 수 있으며 서버로부터 결과값을 받아서 화면에 출력해야한다. 비동기 작업이후 완료된 결과를 출력하기 위해서 데이터를 Binding 해서 RecyclerView에 담아주었다.

그전에 출력 해줄 데이터에 대해서 모델을 제작하였다. 제작한 모델은 아래와 같다.
``` kotlin
data class Hypertension(
    val name: String,
    val count: Int,
    val maxP: Double,
    val minP: Double
)
```

그리고 구현한 data class를 기반으로 Observable한 데이터를 만들어주었다. 왜냐하면 뷰에서 바로 모델을 참조하는 것이 아닌 MVVM 구조로 앱을 설계했기 때문이다. 이로써 원하는 데이터의 상태관리를 효율적으로 해줄 수 있다.

``` kotlin
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HypertensionViewModel: ViewModel() {
    var observableData: MutableLiveData<ArrayList<Hypertension>> = MutableLiveData<ArrayList<Hypertension>>()

    init {
        var hypertensionData = ArrayList<Hypertension>()
        hypertensionData.add(Hypertension("none", 0, 0.0, 0.0))
        hypertensionData.add(Hypertension("none", 0, 0.0, 0.0))
        hypertensionData.add(Hypertension("none", 0, 0.0, 0.0))
        observableData.postValue(hypertensionData)
    }
}
```

총 3개의 데이터를 출력해줄 것이므로 임시 데이터를 넣어주었다. 수정가능한 List를 만들어 주기위해서 data는 MutableLiveData 타입으로 구현하였다.

이제 View에서 출력을 해주면 된다. xml에서 viewModel로 사용할 데이터를 `.kt`와 Data Binding을 해주고 이를 listen 해줄 Observer를 만들어주었다. 

[자세한 내용은 소스코드 참고](https://github.com/seunghwanly/des3776-01-application/blob/main/android/app/src/main/res/layout/activity_main.xml)

## File upload
Android는 다운로드폴더에서 직접적으로 파일을 업로드하는 것을 허용하지 않는다. 따라서 내장폴더에 있는 파일을 캐시폴더로 옮겨 주어야한다. 아래와 같이 데이터를 복사해주었다.

``` kotlin
// convert to inputStream
val inputStream = contentResolver.openInputStream(selectedFileURI!!)!!
val newlyWrittenFile = File(cacheDir.absolutePath + "/" + selectedFileURI!!.port)

// write new file with outputStream
val outputStream = FileOutputStream(newlyWrittenFile)
val buf = ByteArray(1024)
var len: Int
while (true) {
    len = inputStream.read(buf, 0, 1024)
    if (len > 0) {
        outputStream.write(buf, 0, len)
    } else break
}

outputStream.close()
```

그리고 File로 통신을 하기위해서 Retrofit2를 이용한 API Interface도 구현해주었다. 코드는 다음과 같다.

``` kotlin
interface HyperTensionAPI {
    @Multipart
    @POST("hypertensions")
    fun getResultFromFile(
        @Part body: MultipartBody.Part,
    ): Call<JsonObject>
}
```
Retrofit2와 Coroutine을 활용하면 동기 및 비동기작업을 원활하게 진행할 수 있다고 한다. 이는 추후에 추가될 예정이다.

모든 작업이 끝난 후 Retrofit2 instance를 생성해서 위에서 만든 interface에 해당하는 method를 넣어준다. 그 후 `enque`를 이용해서 비동기 방법으로 요청을 해 Main Thread의 부담을 줄여준다. 코드는 아래와 같다.

``` kotlin
fun getEvaluationOfSelectedTestCase(view: View) {
        /// select file first
        val selectFileIntent = Intent().setType("*/*").setAction(Intent.ACTION_GET_CONTENT)
        selectFileIntent.addCategory(Intent.CATEGORY_OPENABLE)
//        selectFileIntent.type = "text/comma-separated-values"
        selectFileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        // execute
        intentLauncher.launch(selectFileIntent)

        if (selectedFileURI != null) {
            // convert to inputStream
            val inputStream = contentResolver.openInputStream(selectedFileURI!!)!!
            val newlyWrittenFile =
                File(cacheDir.absolutePath + "/" + selectedFileURI!!.port)

            // write new file with outputStream
            val outputStream = FileOutputStream(newlyWrittenFile)
            val buf = ByteArray(1024)
            var len: Int
            while (true) {
                len = inputStream.read(buf, 0, 1024)
                if (len > 0) {
                    outputStream.write(buf, 0, len)
                } else break
            }

            outputStream.close()

            Log.d("fileNEW", "wrote everything")

            val requestFile = RequestBody.create(
                MediaType.parse(contentResolver.getType(selectedFileURI!!)!!),
                newlyWrittenFile
            )
            val requestBody =
                MultipartBody.Part.createFormData("file", newlyWrittenFile.name, requestFile)

            // execute the request
            // use Retrofit to create request
            val baseURL = "http://10.0.2.2:3000/"
            val retrofit =
                Retrofit.Builder().baseUrl(baseURL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            val api = retrofit.create(HyperTensionAPI::class.java)
            val call = api.getResultFromFile(requestBody)

            /// use in asychronous way
            call.enqueue(object : Callback<JsonObject> {
                override fun onResponse(
                    call: Call<JsonObject>,
                    response: Response<JsonObject>
                ) {
                    Log.d("onCall", call.isExecuted.toString() + call.toString())
                    val results = response.body()?.getAsJsonArray("result")

                    if (results != null) {
                        val parsedHypertensions: MutableList<Hypertension> = mutableListOf()
                        for (res in results) {
                            val resObject = res.asJsonObject
                            val name = resObject.get("name").asString
                            val count = resObject.get("cnt").asInt
                            val maxP = resObject.get("max_p").asDouble
                            val minP = resObject.get("min_p").asDouble
                            // save to list
                            val item = Hypertension(name, count, maxP, minP)
                            parsedHypertensions.add(item)
                        }
                        Log.d("getEvaluationOfSelectedTestCase", parsedHypertensions.toString())
                        viewmodel.observableData.postValue(parsedHypertensions as ArrayList<Hypertension>?)
                    }
                }

                override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                    Log.d("onRequest", "failed $t")
                }
            })
        }
    }
```

# Result

<img width="467" alt="스크린샷 2021-10-08 오전 11 28 09" src="https://user-images.githubusercontent.com/22142225/136490159-cff58886-7c7b-4559-95cd-887d5e6471e3.png">


# TODO
- `/resource/api.py`

    index의 번호가 주어진 범위를 벗어나게되면 에러를 반환해주어야한다. 이 점은 현재 애플리케이션에서 1-3까지만 요청을 하는 것으로 처리가 된다.

- client
    
    요청이 비동기 방식으로 이루어지기 때문에 사용자에게 보여질 화면이 필요하다.
