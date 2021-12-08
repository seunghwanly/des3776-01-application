# Topic
개별연구(타겟질병의 발병확률 예측 알고리즘을 적용한 어플리케이션 제작)

# Requirements
1. 주어진 SNP를 기반으로 itrc_snp.. file 에서 input으로 들어가는 testcase 별로 geno 0, geno 1 그리고 geno 2의 결과를 출력
2. PC 통신을 할 수 있는 환경에서 앱을 실행

# Service
요구사항을 충족하기 위해서는 파일을 입력할 수 있는 애플리케이션, 즉 클라이언트가 필요하며 클라이언트의 요청을 처리해줄 서버가 필요했다. 따라서 안드로이드 환경에서 구동될 수 있도록 kotlin으로 애플리케이션을 구현하고 요청을 처리하는 서버는 python을 이용해 FastAPI로 구현하였다.

## Server
``` plain
app
├── resource
│   └── api.py
├── service
│   └── prediction_service.py
└── app.py
```
- api
 
사용자가 서버에 접근하는 End-point를 관리한다. class로 구현해 GET, POST, PUT, DELETE등 다양한 method를 구현할 수 있다.

- service

현재는 입력받은 테스트 케이스의 결과값을 반환해주는 작업을 해주고 있다.

- dataset

주어진 데이터를 저장해둔 곳이다.

### API
요청을 처리해줄 Hypertensions 라는 class를 구현하였는데 코드는 아래와 같다.
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

Hypertensions 클래스 내 method는 현재 하나로 이루어져있다. 사용자로부터 입력받을 file을 parsing해서 관련 데이터 값으 service layer로 넘겨준다.

#### POST
사용자로부터 POST 요청이 오게되면 'file'이란 key값을 가지고 있는 multipart/form-data를 확인한다. 그리고 관련 작업은 service 레이어에서 이루어진다. 만약에 file 이라는 data가 첨부되지 않았다면 status code 400과 함께 에러를 반환한다.

##### Result
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

## Client
Android 환경에서 구동되는 애플리케이션을 구현하였다. Kotlin으로 작성되었으며 크게는 3개의 Activity와 MVVM 패턴으로 구성되어있다. 사용자는 크게 자신의 정보를 입력하고 나서 파일을 선택해 서버로 요청을 보낸 뒤 기다리면 서버로 부터 결과를 받아볼 수 있다. 클라이언트 내 주요 기능은 다음과 같다.
- MVVM 구현
- RecyclerView 구현
- File 선택 및 업로드

### MVVM 구성
#### Model
Model-ViewModel-View 에서 사용될 Model, 즉 데이터 클래스를 다음과 같이 생성해주었다.
``` kotlin
data class Hypertension(
    val name: String,
    val count: Int,
    val maxP: Double,
    val minP: Double
): Serializable
```

화면에서 표기될 데이터로 이루어진 클래스이며 리사이클러뷰에도 적용된다.
#### ViewModel
모델과 관련해서 비즈니스 로직이 적용될 부분으로 다음과 같이 작성하였다.
``` kotlin
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

빈 데이터를 3개를 넣어준 것은 geno1, geno2 그리고 geno3을 넣어주기 위함이고, 해당 리스트는 수정될 수 있는 `MutableLiveData`로 선언하였다.

#### View
위와 같이 모델과 뷰모델을 작성 후 이를 listen하는 뷰를 연결시켜주었다.
``` kotlin
private lateinit var viewmodel: HypertensionViewModel
...
// link to Observer
        val dataObserver: Observer<ArrayList<Hypertension>> =
            Observer { observableData ->
                val newAdapter = HypertensionAdapter(this)
                newAdapter.data = observableData
                binding.genoRecyclerview.adapter = newAdapter
                Log.d("Observer", "looking ggod")
            }
        viewmodel.observableData.observe(this, dataObserver)
        setRecyclerView()
        viewmodel.observableData.postValue(receivedData)
```

`onCreate( )` 와 같은 생성주기에서 리사이클러뷰에 뷰모델을 적용하면서 렌더링을 해주었다.

### RecyclerView 구현
뷰에서 관련 화면을 나타낼 줄 수 있는 결과화면을 구성하기 위해서 리사이클러뷰를 사용하였다. 소스코드는 다음과 같다.
``` kotlin
class HypertensionAdapter(private val context: Context) :
    RecyclerView.Adapter<HypertensionAdapter.HypertensionViewHolder>() {

    var data = mutableListOf<Hypertension>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): HypertensionViewHolder {
        val binding = HypertensionsItemBinding.inflate(LayoutInflater.from(context), parent, false)

        return HypertensionViewHolder(binding)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: HypertensionViewHolder, position: Int) {
        holder.onBind(data[position])
    }

    inner class HypertensionViewHolder(private val binding: HypertensionsItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun onBind(data: Hypertension) {
            binding.hypertension = data
        }

    }
}
```
리사이클러뷰의 어댑터를 상속 받으면서 내부 메소드를 오버라이딩해주어 사용하였다. 

### File 선택 및 업로드
파일을 업로드하기 전에 필요한 것은 관련된 API 요청이다. 따라서 이를 위한 인터페이스를 제작하였다. 소스코드는 아래와 같다.
``` kotlin
interface HyperTensionAPI {
    @GET("hypertensions")
    fun getEvaluation(
        @Query("index") index: Int
    ): Call<JsonObject>

    @Multipart
    @POST("hypertensions")
    fun getResultFromFile(
        @Part body: MultipartBody.Part,
    ): Call<JsonObject>
}
```

File을 업로드할 수 있도록 Multipart로 형식을 맞춰주었고 서버와 키값을 통일해주었다. 그리고 파일을 선택하기위해서는 뷰에서 이벤트를 호출해야하는데 `onCreate( )` 단계에서 먼저 해당 인텐트를 설정해주었다.
``` kotlin
intentLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { res ->
            if (res.resultCode == RESULT_OK) {
                selectedFileURI = res.data?.data
                Log.d("onActivityResult", selectedFileURI!!.toString())
                if (selectedFileURI != null) {
                    createRequest()
                }
            }
        }
```
위와 같이 설정된 인텐트는 아래 뷰에서 사용자가 버튼을 눌렀을 때 이벤트를 발생시킨다. 파일 업로드를 하기위해서 새로 추가된 안드로이드 정책에 맞춰서 파일을 해당 디렉토리에서 바로 가져오지 않고 임시 폴더 내로 복사를 한 다음에 요청을 보낸다.

## Results
이렇게 서버와 클라이언트를 모두 구현하여 실행한 결과는 아래와 같다.

### Screenshots

#### Client
<p align="center">
<img width=200 src="https://user-images.githubusercontent.com/22142225/143763372-fb29a5ac-49a9-4262-a167-db8ef939f68f.png"/>
INTRO 화면
<img width=200 src="https://user-images.githubusercontent.com/22142225/143763375-b10e03e9-0716-49d9-9e84-6f7450201700.png"/>
입력 폼 화면
<img width=200 src="https://user-images.githubusercontent.com/22142225/143763379-3e6c4f01-26b3-4dd1-9c04-2e22626df842.png"/>
파일 선택 화면 
<img width=200 src="https://user-images.githubusercontent.com/22142225/143763381-1465a400-f9b0-49ac-a627-40c9efb82bf4.png"/>
파일 선택 후 기다리는 화면
<img width=200 src="https://user-images.githubusercontent.com/22142225/143763382-37dbce35-fb7b-4ee0-839b-ff24280dc1d8.png"/>
입력 받은 결과 출력 화면
</p>

#### Server

![스크린샷 2021-11-28 오후 7 00 30](https://user-images.githubusercontent.com/22142225/143763485-4dd15499-e333-4bd9-ac4a-1cf81b9d29db.png)
서버 실행 화면
