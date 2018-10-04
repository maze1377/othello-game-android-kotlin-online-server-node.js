const schedule = require('node-schedule');
const {Border} = require('../models/border');
const {Onlines, Requests} = require('../models/onlines');
const {User} = require('../models/user');
module.exports = async function () {
    var event = schedule.scheduleJob("*/1 * * * *", async function () {
        const date = new Date();
        const list = User.find().exec(async function (err, docs) {
            for (var i = 0; i < docs.length; i++) {
                if (date - docs[i].date > 30000) {
                    await Onlines.findOneAndUpdate({
                        $pull: {player: {id: docs[i]._id}},
                    });
                    await User.findByIdAndUpdate(docs[i]._id, {
                        $set: {state: "offline"}
                    });
                }
            }
        })
    });

    var rule = new schedule.RecurrenceRule();
    rule.hour = 24;
    schedule.scheduleJob(rule, function () {
        const date = new Data();
        const border = Border.find().exec(async function (err, docs) {
            for (var i = 0; i < docs.length; i++) {
                if (date - docs[i].date > 5000000) {
                    await Border.deleteOne({_id: doc[i]._id});
                }
            }
        })
    });
};