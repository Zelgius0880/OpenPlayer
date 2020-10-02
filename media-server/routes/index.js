const express = require('express');
const router = express.Router();
const fs = require('fs');
const path = require('path');
const mime = require('mime-types')


/* GET home page. */
router.get('/track/:name', function (req, res, next) {

    let filePath = path.join('/Users/florian/Downloads/Music', decodeURIComponent(req.params.name.replace(/\+/g, '%20')));
    let stat = fs.statSync(filePath);

    res.writeHead(200, {
        'Content-Type': mime.lookup(filePath),
        'Content-Length': stat.size
    });

    let readStream = fs.createReadStream(filePath);
    // We replaced all the event handlers with a simple call to readStream.pipe()
    readStream.pipe(res);

});

module.exports = router;
