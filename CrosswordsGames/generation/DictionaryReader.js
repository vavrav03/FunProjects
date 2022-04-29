const fs = require('fs');
const readline = require('readline');
const { MAX_SOLUTION_LENGTH, MIN_SOLUTION_LENGTH } = require('../config');

const dictionary = [];

class WordCouple {
    constructor(word, definition) {
        this.word = word;
        this.definition = definition;
    }
}

function isLetter(c) {
    return c.toLowerCase() != c.toUpperCase();
}

async function readDictionary() {
    const fileStream = fs.createReadStream(
        __dirname + '/../dictionary/dictionary.txt'
    );

    const rl = readline.createInterface({
        input: fileStream,
        crlfDelay: Infinity,
    });

    words: for await (const line of rl) {
        const splitData = line.split('\t');
        const english = splitData[0];
        const czech = splitData[1];
        for (const letter of english) {
            //slovo může obsahovat znaky jako -() či mezera, ty ale v křížovkách nechceme, takže taková slova do slovníku nedáme.
            if (!isLetter(letter)) {
                continue words;
            }
        }
        for (const letter of czech) {
            if (!isLetter(letter)) {
                continue words;
            }
        }
        if (!dictionary[english.length - 1]) {
            //první slovo této délky. Není možné to udělat předem, protože nevíme obecnou délku nejdelšího slova.
            dictionary[english.length - 1] = [];
        }
        dictionary[english.length - 1].push(new WordCouple(english, czech));
    }
    for (let i = 0; i < dictionary.length; i++) {
        //pro 17 nejsou ve slovníku žádná přijatelná slova, takže by zůstal undefined a rozbil program
        if (!dictionary[i]) {
            dictionary[i] = [];
        }
    }
    // for (let i = 0; i < dictionary.length; i++) {
    //    for (let j = 0; j < dictionary[i].length; j++) {
    //       console.log(i, j, dictionary[i][j], dictionary[i][j]);
    //    }
    // }
}

//náhodná tajenka - s ohledem na maximum a minimum její délky
function randomSolution() {
    const wordLength =
        MIN_SOLUTION_LENGTH +
        Math.floor(Math.random() * (MAX_SOLUTION_LENGTH - MIN_SOLUTION_LENGTH));
    const word = randomWordWithLength(wordLength + 1);
    if (word) {
        return word;
    } else {
        return randomSolution();
    }
}

//náhodné slovo s garancí nalezení
function randomWord() {
    const wordLength = Math.floor(Math.random() * dictionary.length); //random nemůže být nikdy 1, takže toto může být
    const word = randomWordWithLength(wordLength + 1);
    if (word) {
        return word;
    } else {
        return randomWord();
    }
}

//náhodné slovo s rozsahem od min do max
function randomWordMinMaxLength(min, max) {
    const wordLength = min + Math.floor(Math.random() * (max - min)); //random nemůže být nikdy 1, takže toto může být
    const word = randomWordWithLength(wordLength + 1);
    if (word) {
        return word;
    } else {
        return randomWord();
    }
}

//náhodné slovo s danou délkou bez garance nalezení (nejde dát. Pokud tam žádné slovo není, není možnost, jak vrátít jiné slovo (problém vzniká u délku slov 17 - žádné tam není))
function randomWordWithLength(wordLength) {
    const index = Math.floor(Math.random() * dictionary[wordLength - 1].length);
    return dictionary[wordLength - 1][index];
}

module.exports = {
    readDictionary,
    randomWord,
    randomSolution,
    randomWordWithLength,
    randomWordMinMaxLength,
};
