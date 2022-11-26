from flask import Flask
# flask run --host=0.0.0.0
app = Flask(__name__)

@app.route('/')
def index():
    return "<p>Hello, World!</p>"


if __name__ == '__main__':
    app.run(debug=True)