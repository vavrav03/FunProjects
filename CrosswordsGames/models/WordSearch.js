const mongoose = require('mongoose');

const WordData = new mongoose.Schema({
    word: String,
    sCol: Number,
    sRow: Number,
    eCol: Number,
    eRow: Number,
});

const WordSearchSchema = new mongoose.Schema({
    words: [WordData],
    sentenceBefore: String,
    sentenceAfter: String,
    solution: String,
    solutionSpaced: String,
    height: Number,
    width: Number,
    usedCount: {
        type: Number,
        default: 0,
    },
});

WordSearchSchema.virtual('grid').get(function () {
    //tento field se do schématu přidá až po načtení z databáze, v ní není, aby se šetřilo místo.
    const grid = new Array(this.height);
    for (let i = 0; i < this.height; i++) {
        grid[i] = new Array(this.width);
    }
    for (let i = 0; i < this.words.length; i++) {
        const wordData = this.words[i];
        const wordString = wordData.word;
        let currentX = wordData.sCol;
        let currentY = wordData.sRow;
        let changeX;
        let changeY;
        if (wordData.sCol > wordData.eCol) {
            changeX = -1;
        } else if (wordData.sCol == wordData.eCol) {
            changeX = 0;
        } else {
            changeX = 1;
        }
        if (wordData.sRow > wordData.eRow) {
            changeY = -1;
        } else if (wordData.sRow == wordData.eRow) {
            changeY = 0;
        } else {
            changeY = 1;
        }
        for (let j = 0; j < wordString.length; j++) {
            grid[currentY][currentX] = wordString[j];
            currentX += changeX;
            currentY += changeY;
        }
    }
    let counter = 0;
    for (let i = 0; i < this.height; i++) {
        for (let j = 0; j < this.width; j++) {
            if (grid[i][j] == null || grid[i][j] == undefined) {
                grid[i][j] = this.solution[counter++];
            }
        }
    }
    return grid;
});
module.exports = mongoose.model('WordSearch', WordSearchSchema);
