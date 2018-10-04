const {Border} = require('../models/border');
const express = require('express');
const router = express.Router();

//creat border (send id to client!)
router.post('/new', async (req, res) => {
    const border = new Border();
    await border.save();
    res.send(border._id);
});
//join game
router.put('/join', async (req, res) => {
    const border = await Border.findByIdAndUpdate(req.body._bord, {
        $push: { player :req.body._id},
        $inc: { color : 1}
    });
    if (!border) return res.status(400).send('border not foind!!!.');
    return res.json({ color : border.color });
});
//todo !!2taii aia omadan to bazi ia na?

//get last move!!
router.put('/move/last', async (req, res) => {
    const border = await Border.findById(req.body._bord).select("+moves");
    if (!border) return res.status(404).send('The border with the given ID was not found.');
    if (border.moves.length==0){
        return res.json({});
    }
    const last=border.moves[border.moves.length-1];
    res.send(last);
});
//put new move... //:id id of border!
router.put('/move/add', async (req, res) => {
    const border = await Border.findByIdAndUpdate(req.body._bord, {
        $push: {moves :{x : req.body.x , y : req.body.y , userId : req.body._id}}});
    if (!border) return res.status(404).send('The border with the given ID was not found.');
    res.send("ok");
});

//get last massage
router.put('/massege/last', async (req, res) => {
    const border = await Border.findById(req.body._bord).select("+masseges");
    if (!border) return res.status(404).send('The border with the given ID was not found.');
    if (border.masseges.length==0){
        return res.json({});
    }
    const last=border.masseges[border.moves.length-1];
    res.send(last);
});
//put massage(send name...)
router.put('/massege/add', async (req, res) => {
    const border = await Border.findByIdAndUpdate(req.body._bord, {
        $push: {masseges : {player : req.body.name , massege : req.body.masseges} }});
    if (!border) return res.status(404).send('The border with the given ID was not found.');
    res.send("ok");
});

module.exports = router;