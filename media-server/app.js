const express = require('express');
const path = require('path');
const cookieParser = require('cookie-parser');
const logger = require('morgan');
const indexRouter = require('./routes/index');
const app = express();
const http = require('http');
const httpProxy = require('http-proxy');
const propertiesReader = require('properties-reader');
const proxyMopidy = httpProxy.createProxyServer({
    host: 'http://127.0.0.1/mopidy',
    port: 6680
});

const proxyLocal = httpProxy.createProxyServer({
    host: 'http://127.0.0.1/local',
    port: 6680
});

const properties = propertiesReader('local.properties');
const key = properties.path().main.key

const admin = require('firebase-admin');

const serviceAccount = require('./firestore-keys.json');

admin.initializeApp({
    credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();

app.use(logger('dev'));
app.use(express.json());
app.use(express.urlencoded({extended: false}));
app.use(cookieParser());
app.use(express.static(path.join(__dirname, 'public')));

app.use(function (req, res, next) {

    let token = req.headers['x-auth-token'];

    //Check if not token
    if (!token) {
        return res.status(401).json({msg: 'No token, authorization denied'});
    } else if (token != key)
        return res.status(401).json({msg: 'Wrong token, authorization denied'});
    else
        next()
});

app.use('/', indexRouter);
app.use(['/player', '/player/*'], (req, res, next) => {
    proxyMopidy.web(req, res, {target: 'http://127.0.0.1:6680/mopidy'}, e => {
        console.log(e)
        next(e)
    });
});

app.use('/', indexRouter);
app.use(['/local', '/local/*'], (req, res, next) => {
    proxyLocal.web(req, res, {target: 'http://127.0.0.1:6680/local'}, e => {
        console.log(e)
        next(e)
    });
});

const EventEmitter = require('events').EventEmitter,
    spawn = require('child_process').spawn,
    rl = require('readline');

const RE_SUCCESS = /bytes from/i,
    INTERVAL = 2, // in seconds
    IP = '8.8.8.8';

const proc = spawn('ping', ['-v', '-n', '-i', INTERVAL, IP]),
    rli = rl.createInterface(proc.stdout, proc.stdin),
    network = new EventEmitter();

network.online = false;

rli.on('line', function (str) {
    if (RE_SUCCESS.test(str)) {
        if (!network.online) {
            network.online = true;
            network.emit('online');
        }
    } else if (network.online) {
        network.online = false;
        network.emit('offline');
    }
});

// then just listen for the `online` and `offline` events ...
network.on('online', function () {
    console.log('online!');

    refreshIp().then(s => console.log(s))
        .catch(reason => console.error(reason))
}).on('offline', function () {
    console.log('offline!');
});

proxyMopidy.on('proxyReq', (proxyReq, req) => {
    if (req.body) {
        const bodyData = JSON.stringify(req.body);
        // incase if content-type is application/x-www-form-urlencoded -> we need to change to application/json
        proxyReq.setHeader('Content-Type', 'application/json');
        proxyReq.setHeader('Content-Length', Buffer.byteLength(bodyData));
        // stream the content
        proxyReq.write(bodyData);
    }
});

var ip = '';

function refreshIp() {
    return new Promise(async (resolve, reject) => {
        const docRef = db.collection('ips').doc('open-player');

        ip = '';
        http.get('http://checkip.amazonaws.com', res => {

            //another chunk of data has been received, so append it to `str`
            res.on('data', function (chunk) {
                ip += chunk;
            })
                .on('end', async () => {

                    await docRef.set({
                        ip: ip
                    });

                    resolve(ip)
                })
                .on('error', err => reject(err))
        })

    })

}
/*
http.createServer(function(req, res) {
    res.writeHead(200, {
        'Content-Type': 'text/plain'
    });
    res.end('Hello my public ip is '+ ip +'\n');
}).listen(3000);*/

module.exports = app;
