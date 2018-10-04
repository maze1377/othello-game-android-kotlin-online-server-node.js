const Joi = require('joi');
const mongoose = require('mongoose');

const userSchema = new mongoose.Schema({
    name: {
        type: String,
        required: true,
        minlength: 3,
        maxlength: 30,
        unique: true
    },
    email: {
        type: String,
        minlength: 5,
        maxlength: 255,
        unique: true
    },
    password: {
        type: String,
        minlength: 5,
        maxlength: 1024
    },
    coins: {
        type : Number,
        default: 50
    },
    massege :[{
        sender: {
            type: String,
            default: "system"
        },
        text : String,
        isSend : {
            type: Boolean,
            default: false
        },
        mode :{
            type: String,
            default: "system"
        },
        answer: String,
        date :{
            type: Date,
            default: Date.now
        }
    }],
    paysinfo :[{
        text : String,
        isUsed : {
            type: Boolean,
            default: false
        },
        data : Date
    }],
    friends:[{
        type: mongoose.Schema.Types.ObjectId,
        ref : 'user'
    }],
    state : {
       type :String,
        default: "offline"
    },
    checker : {
        massage :
            {
            type: Boolean,
            default: false
        }
    },
    isUser : {
        type: Boolean,
        default: false
    },
    isGold: {
        type: Boolean,
        default: false
    },
    date: { type: Date, default: Date.now }
});

const User = mongoose.model('user', userSchema);

function validateUser(user) {
    const schema = {
        name: Joi.string().min(3).max(50).required(),
        email: Joi.string().min(5).max(255).required().email(),
        password: Joi.string().min(5).max(255).required(),
        isUser :Joi.boolean(),
        Coins : Joi.number().integer()
    };
    return Joi.validate(user, schema);
}

exports.User = User;
exports.validate = validateUser;