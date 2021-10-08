from flask import current_app, g
from flask_pymongo import PyMongo
from flask_restful import Resource, reqparse
import werkzeug

from services.prediction_service import HypertensionService


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