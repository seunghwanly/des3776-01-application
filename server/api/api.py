from flask_restful import Resource, reqparse
from service.prediction_service import create_dictionary, evaluate_testcase

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