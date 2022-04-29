const express = require('express');
const app = express();
const { readDictionary } = require('./generation/DictionaryReader');
const { generateWordSearch } = require('./generation/WordSearchGenerator');
const { generateCrosswords } = require('./generation/CrosswordsGenerator');
const {
    DB_URL,
    UNACCEPTABLE_USE_COUNT,
    ENOUGH_CROSSWORDS_GAMES,
    ENOUGH_WORD_SEARCH_GAMES,
} = require('./config');

//načtení databázových knihoven, modelů a vytvoření DB spojení
const mongoose = require('mongoose');
const Crosswords = require('./models/Crosswords');
const WordSearch = require('./models/WordSearch');
mongoose.connect(DB_URL, { useNewUrlParser: true });
const db = mongoose.connection;
db.on('error', (error) => console.error(error));
db.once('open', () => console.log('Connected to Mongo'));

app.set('view engine', 'ejs');
app.use(express.static('public'));

app.get('/', function (req, res) {
    res.render('index');
});

app.get('/crosswords_separated', function (req, res) {
    res.render('crosswords_separated', {});
});

app.get('/crosswords', async function (req, res) {
    const count = await Crosswords.count();
    const randomItemsToSkip = Math.floor(Math.random() * count);
    const item = await Crosswords.findOne().skip(randomItemsToSkip);
    await res.render('crosswords', {
        task: item,
    });
    if (item.usedCount >= UNACCEPTABLE_USE_COUNT) {
        console.log('removing');
        item.remove();
        generateCrosswords().save();
    } else {
        item.usedCount++;
        item.save();
    }
});

app.get('/word_search', async function (req, res) {
    const count = await WordSearch.count();
    const randomItemsToSkip = Math.floor(Math.random() * count);
    const item = await WordSearch.findOne().skip(randomItemsToSkip);
    await res.render('word_search', {
        task: item,
    });
    if (item.usedCount >= UNACCEPTABLE_USE_COUNT) {
        //generate
    } else {
        item.usedCount++;
        item.save();
    }
});

//metoda zajistí, že je v databázi dost křížovek i osmisměrek. Jestliže ne, potřebný počet doplní
async function ensureEnoughGames() {
    const crosswordsCount = await Crosswords.count();
    if (crosswordsCount < ENOUGH_CROSSWORDS_GAMES) {
        for (let i = crosswordsCount; i < ENOUGH_CROSSWORDS_GAMES; i++) {
            generateCrosswords().save();
        }
    }
    const wordSearchCount = await WordSearch.count();
    if (wordSearchCount < ENOUGH_WORD_SEARCH_GAMES) {
        for (let i = wordSearchCount; i < ENOUGH_WORD_SEARCH_GAMES; i++) {
            generateWordSearch().save();
        }
    }
}

async function initServer() {
    await readDictionary();
    await ensureEnoughGames();
    app.listen(8080, 'localhost', () => {
        //server se musí spustit až po načtení slovníku, jinak by ve zlomku vteřiny mohla nastat nekonzistence při generování nové hry.
        console.log('running server');
    });
}
initServer();