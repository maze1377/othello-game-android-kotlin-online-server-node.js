//todo add upload file services...
const express = require('express');
const router = express.Router();
const {Border} = require('../models/border');
const {Onlines, Requests} = require('../models/onlines');
const {User} = require('../models/user');
//admin page:
router.get('/home', function (req, res) {
    res.sendFile("/static/image/index.html");//todo relation address...
});
//upload page response
router.post("/upload", function (req, res) {
    if (!req.files)
        return res.status(400).send('No files were uploaded.');
    let sampleFile = req.files.filename.name;
    // sampleFile.mv('./static/image/'+sampleFile, function(err) {
    //     if (err) {
    //         return res.status(500).send(err);
    //     }
    res.send("done...!");
});
router.put("/delete", async (req, res) => {
    if (req.body.code == "clean") {
        await Border.deleteMany();
        await User.deleteMany();
        await Onlines.deleteMany();
        await Requests.deleteMany();
        const start = await new Onlines();
        await start.save();
    }
    res.send("done...!");
});

module.exports = router;