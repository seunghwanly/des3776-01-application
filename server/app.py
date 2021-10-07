from flask import Flask
from flask_pymongo import PyMongo
from flask_restful import Api

from api.api import Hypertensions
from service.prediction_service import HypertensionService

app = Flask(__name__)

# config
app.config["MONGO_URI"] = 'mongodb://localhost:27017/des3776'

# init API
api = Api(app)
api.add_resource(Hypertensions, '/hypertensions')

if __name__ == "__main__":
    app.run(port=3000, debug=True)