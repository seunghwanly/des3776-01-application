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
├── api
│   └── api.py
├── service
│   └── prediction_service.py
├── dataset
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
class Hypertensions(Resource):
    """methods with Hypertensions

    Methods
    -------
    get()
        returns the result of geno 0, geno 1 and geno 2 evaluted
        which is based on itrc_snp_hypertension_sm
    """
    def __init__(self):
        ref = create_dictionary()
        self.ref = ref

    def get(self):
        try:
            # parse request
            parser = reqparse.RequestParser()
            parser.add_argument('index', required=True, type=int, help='place index of the testcase')
            args = parser.parse_args()
            # get result
            g0, g1, g2 = evaluate_testcase(f"./dataset/test_set{args['index']}.csv", self.ref)
            return {'result': {'geno0': g0, 'geno1': g1, 'geno2': g2}}
        except Exception as e:
            return {'result': str(e)}, 400
```

직접적으로 Api layer에서 연산을 다루지 않고 service layer로의 호출만이 이루어진다. 아직은 따로 함수들을 나누지는 않았지만 추후 업데이트 예정이다.

`Hypertensions` 클래스 내 method는 현재 하나로 이루어져있다. 클래스 내 생성자에서는 Hypertensions내에서 다룰 reference를 `ref`로 property를 하나 생성해 사용한다. ref 를 생성할 때도 service layer에서 호출해 사용한다.

### GET 
생성된 ref를 가지고 사용자로부터 GET 요청이 오게되면 입력된 parameter를 확인한다. 만약에 `index` 라는 parameter가 입력되지 않았다면 status code 400과 함께 에러를 반환한다. 

현재 주어진 테스트 케이스는 3개이므로 입력된 index에 따라서 결과값을 반환하게 된다. 

#### TODO
index의 번호가 주어진 범위를 벗어나게되면 에러를 반환해주어야한다. 이 점은 현재 애플리케이션에서 1-3까지만 요청을 하는 것으로 처리가 된다.

#### Result
위와 같이 작성한 클래스로 요청을 아래와 같이 보낼 수 있다.

``` bash
localhost:5000/hypertensions?index=1
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