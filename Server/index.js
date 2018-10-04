const express = require('express');
const app = express();
require('./startup/cleaner')();
//require('./startup/logging')();
require('./startup/routes')(app);
require('./startup/db')();
require('./startup/prod')(app);

app.get('/test',(req, res) => {
    return res.send("it working...!!");
});

const port = process.env.PORT || 4080;
const server = app.listen(port, () => console.log(`Listening on port ${port}...\n...!!!`));
module.exports = server;