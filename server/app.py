from flask import Flask
from flask_restful import Api

from api.api import Hypertensions


app = Flask(__name__)
api = Api(app)

"""add end point"""
api.add_resource(Hypertensions, '/hypertensions')

if __name__ == "__main__":
    app.run()