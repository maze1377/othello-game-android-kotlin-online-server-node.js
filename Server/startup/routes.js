const express = require('express');
const users=require('../routes/users');
const border=require('../routes/border');
const onlines=require('../routes/online');
const static=require('../routes/static');
const admin=require('../routes/admin');
const mid=require('./middlework');
const upload=require('express-fileupload')
const trimRequest = require('trim-request');

module.exports = function(app) {
    app.use(express.json());
    app.use(mid);
    app.use(trimRequest.body);
    app.use('/api/users', users);
    app.use('/api/border', border);
    app.use('/api/online',onlines);
    app.use('/static',static);
    app.use('/admin',admin);
};