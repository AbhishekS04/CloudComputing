from flask import Flask
app = Flask(__name__)

@app.route("/")
def home():
    return """
    <h1>Welcome to Cloud Computing Lab 8</h1>
    <p>Application Version 2</p>
    <p>Successfully deployed using AWS Elastic Beanstalk.</p>
    """

if __name__ == "__main__":
    app.run(host="127.0.0.1", port=8080)
