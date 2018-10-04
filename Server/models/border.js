const mongoose = require('mongoose');

const borderSchema = new mongoose.Schema({
    player :[{
        type: mongoose.Schema.Types.ObjectId,
        ref : 'user'
    }],
    moves : [{x:String , y :String , userId : String}],
    masseges :[{
        player : String , massege :String
    }],
    color : {
      type : Number,
      default: 1
    },
    date: { type: Date, default: Date.now }
});

const Border = mongoose.model('border', borderSchema);

exports.Border = Border;