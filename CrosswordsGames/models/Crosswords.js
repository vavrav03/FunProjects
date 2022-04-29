const mongoose = require('mongoose');

const PartSchema = new mongoose.Schema({
    word: String,
    definition: String,
    solutionIndex: Number,
});

const CrosswordsSchema = new mongoose.Schema({
    parts: [PartSchema],
    sentenceBefore: String,
    sentenceAfter: String,
    solutionSpaced: String,
    usedCount: {
        type: Number,
        default: 0,
    },
});

module.exports = mongoose.model('Crosswords', CrosswordsSchema);
