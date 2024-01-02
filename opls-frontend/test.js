const express = require("express");
const app = express();
const port = 8081;
const cors = require("cors");

app.use(cors());
app.use(express.urlencoded({
    extended: true
}));
app.use(express.json());

app.use(function(req, res, next) {
    res.header('Access-Control-Allow-Origin', "*");
    res.header('Access-Control-Allow-Methods', 'GET,PUT,POST,DELETE,PATCH');
    res.header("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept, x-client-key, x-client-token, x-client-secret, Authorization");
    next();
});

app.post("/token", (req, res) => {
    res.header("Access-Control-Allow-Origin", "*");

    if (req.body["grant_type"] == "password") {
        if (!(req.body["username"] == "admin" && req.body["password"] == "pw123")) {
            console.log("Bad", req.body["grant_type"]);
            res.status(401).send({
                "error": "bruh"
            });
            return;
        }
    }

    console.log("Good", req.body["grant_type"]);
    res.status(200).send({
        "access_token": "hello",
        "refresh_token": "refresh_me",
        "expires_on": new Date().getTime() + 60 * 60 * 1000,
    });
});

app.get("/account", (req, res) => {
    if (req.header("Authorization")) {
        res.status(200).send({
            "claims": ["CUSTOMER", "ADMIN", "EMPLOYEE"]
        });
    }
    else {
        req.status(401).send({
            "error": "Missing authorization header."
        });
    }
});

app.get("/spot/*", (req, res) => {
    res.status(401).send({
        "error": "Unauthorized."
    });
});

app.use((req, res) => {
    res.status(404).send({
        "message": "not found."
    });
});

app.listen(port, () => {
    console.log(`Listening on ${port}`);
});
