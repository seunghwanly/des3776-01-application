from flask import Flask
from flask_restful import Api

from resources.hypertensions import Hypertensions

app = Flask(__name__)

# config
app.config["MONGO_URI"] = 'mongodb://localhost:27017/des3776'
app.config["FLASK_ENV"] = 'development'
# init API
api = Api(app)
api.add_resource(Hypertensions, '/hypertensions')

if __name__ == "__main__":
    app.run(port=3000, debug=True)