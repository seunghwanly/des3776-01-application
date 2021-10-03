from flask import Flask
from flask_restful import Resource, Api, reqparse
import pandas as pd
import ast
import sys

from service.prediction_service import create_dictionary, evaluate_testcase

# custom functions

app = Flask(__name__)
api = Api(app)

class Hypertensions(Resource):
    def __init__(self):
        ref = create_dictionary()
        self.set_ref(ref)

    def set_ref(self, ref):
        self.ref = ref

    def get(self):
        g0, g1, g2 = evaluate_testcase('./dataset/test_set1.csv', self.ref)
        
        return {
            'result': {
                'geno0': g0,
                'geno1': g1,
                'geno2': g2
            }
        }


        

api.add_resource(Hypertensions, '/hypertensions')

if __name__ == "__main__":
    ref = create_dictionary()
    app.run()