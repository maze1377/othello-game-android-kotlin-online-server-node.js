const express = require('express');
const router = express.Router();
const {Onlines, Requests} = require('../models/onlines');
const {Border} = require('../models/border');
const {User} = require('../models/user');

//get all online player and massages
function shuffle(a) {
    for (let i = a.length - 1; i > 0; i--) {
        const j = Math.floor(Math.random() * (i + 1));
        [a[i], a[j]] = [a[j], a[i]];
    }
    return a;
}

router.put('/all', async (req, res) => {
    const player = await Onlines.findOne();
    const temp = [];
    var j = 0;
    for (var i = 0; i < player.player.length; i++) {
        if (player.player[i].request == false && player.player[i].id != req.body._id) {
            temp.push({id: player.player[i].id, name: player.player[i].name || "not set"});
        }
    }
    var max = 10;
    if (temp.length < max) {
        max = temp.length;
    }
    const result = {};
    result.player = shuffle(temp).slice(0, max);
    res.send(result);
});
//get masage
router.get('/massage/all', async (req, res) => {//done
    const massage = await Onlines.findOne().select('+massege -player -_id');
    res.send(massage.massege.reverse());
});
router.get('/massage/last', async (req, res) => {//done
    const massage = await Onlines.findOne().select('+massege -player -_id');
    res.send(massage.massege.reverse().slice(0, 15));
});
//put massage
router.put('/massage', async (req, res) => {//done
    await Onlines.findOneAndUpdate({
        $push: {massege: {name: req.body.name, text: req.body.text}}
    });
    return res.json({status: 'done'});
});
//add online player
router.put('/addplayer', async (req, res) => {//done
    const players = await Onlines.findOne();
    for (var i = 0; i < players.player.length; i++) {
        if (players.player[i].id == req.body._id) {
            return res.json({status: 'done'});
        }
    }
    await Onlines.findOneAndUpdate({
        $push: {player: {id: req.body._id, name: req.body.name}},
    });
    return res.json({status: 'done'})
});
//remove online player
router.put('/removeplayer', async (req, res) => {//done
    await Onlines.findOneAndUpdate({
        $pull: {player: {id: req.body._id, name: req.body.name}},
    });
    return res.json({status: 'done'});
});
//check state :
router.put('/state', async (req, res) => {
    const players = await Onlines.findOne();
    for (var i = 0; i < players.player.length; i++) {
        if (players.player[i].id == req.body._id) {
            return res.json({answer: players.player[i].request});
        }
    }
    return res.json({answer: 'false'});
});
//
router.put('/codereq', async (req, res) => {
    const allplayer = await Onlines.findOne();
    var code = null;
    for (var i = 0; i < allplayer.player.length; i++) {
        if (allplayer.player[i].id == req.body._id) {
            code = allplayer.player[i].code;
            break;
        }
    }
    if (code == null) return res.status(400).send('you have not any req');
    const request = await Requests.findById(code);
    var name = "null";
    var secid;
    if (request.player[0].id == req.body._id) {
        secid = request.player[1].id;
    } else {
        secid = request.player[0].id;
    }
    const targetuser = await User.findById(secid);
    name=targetuser.name;
    return res.json({code: code, username: name});
});
//send requset for player
router.put('/addrequiest', async (req, res) => {
    const allplayer = await Onlines.findOne();
    for (var i = 0; i < allplayer.player.length; i++) {
        if (allplayer.player[i].id == req.body.target) {
            if (allplayer.player[i].request == true) {
                return res.status(400).send('user busy...');
            }
        }
    }
    const requiest = new Requests({
        player: [{id: req.body._id}, {id: req.body.target}],
        state: "wait"
    });
    await requiest.save();
    for (var i = 0; i < allplayer.player.length; i++) {
        if (allplayer.player[i].id == req.body._id) {
            allplayer.player[i].request = true;
            allplayer.player[i].code = requiest._id;
            break;
        }
    }
    for (var i = 0; i < allplayer.player.length; i++) {
        if (allplayer.player[i].id == req.body.target) {
            allplayer.player[i].request = true;
            allplayer.player[i].code = requiest._id;
            break;
        }
    }
    await allplayer.save();
    return res.json({code: requiest._id});
});
//state mire to halate wait..
router.put('/requieststate', async (req, res) => {
    const request = await Requests.findById(req.body._bord);
    //todo handle exit code...
    return res.json({answer: request.state});
});
//req info
router.put('/reqinfo', async (req, res) => {
    const request = await Requests.findById(req.body._bord);
});
//javabe request ke braia bazi rafte..
router.put('/javab', async (req, res) => {
    if (req.body.state == true) {
        const border = new Border();
        await border.save();
        const request = await Requests.findByIdAndUpdate(req.body._bord, {
            $set: {
                state: "yes",
                code: border._id
            }
        });
        res.send("done");
    } else {
        const request = await Requests.findByIdAndUpdate(req.body._bord, {
            $set: {state: "no"}
        });

        res.send("done");
    }
});
//javab no
router.put('/javab/no', async (req, res) => {
    const request = await Requests.findById(req.body._bord);
    const allplayer = await Onlines.findOne();
    for (var i = 0; i < allplayer.player.length; i++) {
        if (allplayer.player[i].id == request.player[0].id) {
            allplayer.player[i].request = false;
            break;
        }
    }
    for (var i = 0; i < allplayer.player.length; i++) {
        if (allplayer.player[i].id == request.player[1].id) {
            allplayer.player[i].request = false;
            break;
        }
    }
    await allplayer.save();
    return res.json({status: 'done'});
});
// javab yes
router.put('/javab/yes', async (req, res) => {
    const request = await Requests.findById(req.body._bord);
    return res.json({code: request.code});
});
//add to automach

module.exports = router;