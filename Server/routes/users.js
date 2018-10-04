const bcrypt = require('bcrypt');
const _ = require('lodash');
const {User, validate} = require('../models/user');
const express = require('express');
const router = express.Router();
const Joi = require('joi');

// get _id user :(email and pass)
router.put('/login', async (req, res) => {
    let user = await User.findOne({email: req.body.email});
    if (!user) return res.status(400).send('User email not find!!!...');
    const validatepass = await bcrypt.compare(req.body.password, user.password);
    if (!validatepass) return res.status(400).send('password error!!!...');
    res.send(_.pick(user, ['_id','name']));
});

//get user info..(send _id in reg..)
router.put('/me', async (req, res) => {
    const user = await User.findById(req.body._id).select('-password -friends');
    if (!user) return res.status(400).send('User  with _id not find!!!...');
    res.send(user);
});

//update user info..(send _id in reg..)
router.put('/update', async (req, res) => {
    const user = await User.findById(req.body._id).select('-password');
    if (!user) return res.status(400).send('User not foind!!!.');
    if (user.isUser == false) {
        return res.status(400).send('guest can not change anythings!! please regester....');
    }
    let secned = null;
    const schema = {
        name: Joi.string().min(3).max(50),
        email: Joi.string().min(5).max(255).email(),
        password: Joi.string().min(5).max(255)
    };
    const error = Joi.validate(req.body, schema);
    if (error) return res.status(400).send(error.details[0].message);
    if (req.body.name != null) {
        secned = await User.findOne({name: req.body.name});
        if (secned) return res.status(400).send('User with this name already registered.');
        User.findByIdAndUpdate(req.body._id, {
            $set:
                {
                    date: req.body.name
                }
        });
    }
    if (req.body.email != null) {
        let secned = await User.findOne({email: req.body.email});
        if (secned) return res.status(400).send('User email already registered.');
        User.findByIdAndUpdate(req.body._id, {
            $set:
                {
                    email: req.body.email
                }
        });
    }
    if (req.body.password != null) {
        const salt = await bcrypt.genSalt(10);
        User.findByIdAndUpdate(req.body._id, {
            $set:
                {
                    password: await bcrypt.hash(req.body, salt)
                }
        });
    }
    res.send(user);
});
//creat new user..
router.post('/create', async (req, res) => {
    if (!req.body.isUser) {
        let user = await User.findOne({email: req.body.name});
        if (user) return res.status(400).send('User name already registered.');
        user = new User(_.pick(req.body, ['name']));
        await user.save();
        return res.send(_.pick(user, ['_id', 'isUser', 'name', 'isGold', 'Coins']));
    }
    const {error} = validate(req.body);
    if (error) return res.status(400).send(error.details[0].message + " error validate!!");
    let user = await User.findOne({email: req.body.email});
    if (user) return res.status(400).send('User email already registered.');
    user = await User.findOne({name: req.body.name});
    if (user) return res.status(400).send('User name already registered.');
    user = new User(_.pick(req.body, ['name', 'email', 'password', 'isUser', 'isGold', 'Coins']));
    const salt = await bcrypt.genSalt(10);
    user.password = await bcrypt.hash(user.password, salt);
    await user.save();
    res.send(_.pick(user, ['_id', 'name', 'isUser', 'email', 'isGold', 'Coins']));
});

router.get('/coins', async (req, res) => {
    const coins = await User.findById(req.body._id).select('+coins');
    if (!coins) return res.status(400).send('User with this _id not find!!!...');
    res.send(coins);
});
//update coins...(send _id in reg and changes..)
router.post('/coins', async (req, res) => {
    const user = await User.findByIdAndUpdate(req.body._id, {
        $inc: {coins: req.body.change}
    }, {new: true});
    if (!user) return res.status(400).send('User with this _id not find!!!...');
    res.send(user.coins);
});

//get massage...(send _id in reg..)
router.put('/massage', async (req, res) => {
    const user = await User.findById(req.body._id);
    if (!user) return res.status(400).send('User with this _id not find!!!...');
    const result = {
        massege: []
    };
    var j = 0;
    for (var i = 0; i < user.massege.length; i++) {
        if (user.massege[i].isSend == false) {
            result.massege[j++] = user.massege[i];
            user.massege[i].isSend = true;
        }
    }
    user.save();
    result.state = j;
    res.send(result);
});

//get all massage.
router.put('/massages/get', async (req, res) => {
    const massage = await User.findById(req.body._id).select('+massege');
    if (!massage) return res.status(400).send('User with this _id not find!!!...');
    res.send(massage);
});

//add massage
router.put('/massage/put', async (req, res) => {//todo bug dare onam che bug kirie...
    const user = await User.findByIdAndUpdate(req.body._id,
        {
            $push: {massege: {text: req.body.text}}
        });
    if (!user) return res.status(400).send('User not foind!!!.');
    return res.json({status: 'done'})
});

//get frinds..(send _id in reg..)
router.put('/friends/get', async (req, res) => {//done
    const friend = await User.findById(req.body._id).populate('friends');
    if (!friend) return res.status(400).send('User not foind!!!.');
    var result = {};
    result.friends = friend.friends.map(obj => {
        return {
            _id: obj._id,
            email: obj.email,
            name: obj.name,
            state: obj.state
        }
    });
    res.send(result);
});
//barasi request ha
router.put('/friends/req', async (req, res) => {//done
    if (req.body.target == req.body.email) return res.status(400).send('you cant be your own friends');
    const sendreq = await User.findOneAndUpdate({email: req.body.target}, {
        $push: {
            massege: {
                sender: req.body.email + "",
                text: "hi!\nI want to be your friend...",
                mode: "friendreq"
            }
        },
        $set: {
            checker: {
                massage: true
            }
        }
    });
    return res.json({status: 'done'})
});
//delete
router.put('/friends/delete', async (req, res) => {
    const user = await User.findOne({email: req.body.email});
    const user2 = await User.findOne({email: req.body.target});
    if (user == null || user2 == null) return res.status(400).send('User not foind!!!.');
    if (user._id == user2._id) return res.status(400).send('you cant be your own friends');
    await User.findByIdAndUpdate(user._id, {
        $pull: {friends: user2._id}
    });
    await User.findByIdAndUpdate(user2._id, {
        $pull: {friends: user._id}
    });
    return res.json({status: 'done'})
});
//yes to frindes
router.put('/friends/answer', async (req, res) => {
    const user = await User.findOne({email: req.body.email});
    const user2 = await User.findOne({email: req.body.target});
    if (user == null || user2 == null) return res.status(400).send('User not foind!!!.');
    if (user._id == user2._id) return res.status(400).send('you cant be your own friends');
    for (var i = 0; i < user.friends.length; i++) {
        if (user.friends[i].toString().trim() === user2._id.toString().trim()) {
            return res.status(400).json({status: 'done'});
        }
    }
    await User.findByIdAndUpdate(user._id, {
        $push: {friends: user2._id}
    });
    await User.findByIdAndUpdate(user2._id, {
        $push: {friends: user._id}
    });
    return res.json({status: 'done'})
});

//get last pays
router.get('/pays', async (req, res) => {
    const pays = await User.findById(req.body._id).select('+paysinfo');
    res.send(pays);
});
//add massage
router.put('/pays', async (req, res) => {
    const user = await User.findByIdAndUpdate(req.body._id, {
        $push: {paysinfo: {text: req.body.text}},
    });
    if (user) return res.status(400).send('User not foind!!!.');
    return res.json({status: 'done'})
});


//get state of friends player(send _id in reg..)
router.get('/state', async (req, res) => {
    const state = await User.findById(req.body._id).select('+state');
    res.send(state);
});
//send new state
router.put('/state', async (req, res) => {
    const user = await User.findByIdAndUpdate(req.body._id, {
        $set: {state: req.body.state}
    });
    if (!user) return res.status(400).send('User not foind!!!.');
    //redirect to online state...
    // req.body.name = user.name;
    // if (req.body.state == 'online') {//todo test
    //     return res.redirect(307,'/api/online/addplayer');
    // } else {
    //     return res.redirect(307,'/api/online/removeplayer');
    // }
    return res.json({status: 'done'});
});
module.exports = router;