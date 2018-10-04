const mongoose = require('mongoose');

const onlines = new mongoose.Schema({
    player: [{
        id: String,
        name: {
            type: String,
            minlength: 3,
            maxlength: 30,
        },
        request: {
            type: Boolean,
            default: false
        },
        code : {
            type :String,
            default : null
        }
    }],
    massege: [{
        name: String,
        text: String
    }]
});
const Onlines = mongoose.model('onlines', onlines);
const requstSchema = new mongoose.Schema({
    player: [{
        id: String,
    }],
    state: {
        type: String
    },
    code: {
        type: String
    },
    date: {type: Date, default: Date.now}
});
const Requests = mongoose.model('requsets', requstSchema);
const automatch = new mongoose.Schema({
    player: [{
        id: String,
    }],
    state: {
        type: String,
        default : "wait"
    },
    code: {
        type: String
    },
});
const Automatch = mongoose.model('automatch', automatch);
exports.Requests = Requests;
exports.Onlines = Onlines;