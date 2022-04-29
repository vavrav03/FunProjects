const WordSearch = require('../models/WordSearch');
const {
    MIN_WORD_SEARCH_SIZE,
    MAX_WORD_SEARCH_SIZE,
    MAX_SOLUTION_LENGTH,
} = require('../config');
var _ = require('lodash');
const {
    randomWordMinMaxLength,
    randomWordWithLength,
} = require('./DictionaryReader');

let possibleStarts;

function generateWordSearch() {
    const rowCount =
        MIN_WORD_SEARCH_SIZE +
        Math.floor(
            Math.random() * (MAX_WORD_SEARCH_SIZE - MIN_WORD_SEARCH_SIZE)
        );
    const colCount =
        MIN_WORD_SEARCH_SIZE +
        Math.floor(
            Math.random() * (MAX_WORD_SEARCH_SIZE - MIN_WORD_SEARCH_SIZE)
        );
    possibleStarts = [];
    for (let i = 0; i < rowCount; i++) {
        for (let j = 0; j < colCount; j++) {
            possibleStarts.push({
                row: i,
                col: j,
            });
        }
    }
    const wordSearchBase = {
        words: [],
        grid: [],
        height: rowCount,
        width: colCount,
    };
    for (let i = 0; i < rowCount; i++) {
        wordSearchBase.grid.push([]);
        for (let j = 0; j < colCount; j++) {
            wordSearchBase.grid[i][j] = null;
        }
    }
    const wordSearch = recurse(wordSearchBase);
    //VYPISOVÁNÍ PRO TESTOVÁNÍ
    // for(let i = 0; i < wordSearch.grid.length; i++){
    //    let stringS = ""
    //    for(let j = 0; j < wordSearch.grid[i].length; j++){
    //       stringS += (wordSearch.grid[i][j] ? wordSearch.grid[i][j] : "-") + " ";
    //    }
    //    console.log(stringS)
    // }
    // for(let i = 0; i < wordSearch.words.length; i++){
    //    console.log(wordSearch.words[i]);
    // }
    return new WordSearch(wordSearch);
}

function recurse(wordSearch) {
    for (let i = 0; i < 6; i++) {
        //Tolik slov bude vyzkoušeno, než bude krok výše zahozen jako špatný (6 - hodnota je umělá, lze ji libovolně změnit)
        const newWordSearch = _.cloneDeep(wordSearch); //je třeba vytvořit klon, aby nedocházelo ke konfliktům mezi rodiči a dětmi při rekurzi
        newWordSearch.words.push({
            word: newUniqueWord(
                newWordSearch,
                Math.max(
                    newWordSearch.grid.length,
                    newWordSearch.grid[0].length
                )
            ),
        }); //bereme to tak, že dané slovo bude přidáno. Jestliže ne, prostě se dále s klonem newWordSearch nebude pracovat
        let possibleStartsLocal = []; //všechny možnost, kam umístit první písmeno slova
        for (const el of possibleStarts) {
            possibleStartsLocal.push({ row: el.row, col: el.col });
        }
        while (possibleStartsLocal.length != 0) {
            const randomIndex = Math.floor(
                Math.random() * possibleStartsLocal.length
            ); //náhodný výběr pozic - dobré zadání
            const start = possibleStartsLocal[randomIndex];
            newWordSearch.words[newWordSearch.words.length - 1].sRow =
                start.row;
            newWordSearch.words[newWordSearch.words.length - 1].sCol =
                start.col;
            possibleStartsLocal.splice(randomIndex, 1);
            if (
                tryRightUp(newWordSearch) ||
                tryRightDown(newWordSearch) ||
                tryLeftDown(newWordSearch) ||
                tryUp(newWordSearch) ||
                tryLeftUp(newWordSearch) ||
                tryRight(newWordSearch) ||
                tryDown(newWordSearch) ||
                tryLeft(newWordSearch)
            ) {
                //zkoušení všech způsobů, jak z daného políčka položit slovo. Pokud to jde, je položeno do gridu (vždy se kvůli chování || vykoná jen 1 položení)
                const emptyLetterCount = countEmptyLetters(newWordSearch);
                if (emptyLetterCount <= MAX_SOLUTION_LENGTH) {
                    //řešení nalezeno
                    addSolution(newWordSearch, emptyLetterCount);
                    return newWordSearch;
                }
                const result = recurse(newWordSearch);
                if (result) {
                    //ukončení rekurze
                    return result;
                } else {
                    break;
                }
            }
        }
        // newWordSearch.words.splice(newWordSearch.words.length - 1, 1);
    }
    return null;
}

function countEmptyLetters(wordSearch) {
    //spočítá, kolik písmen chybí (na dané pozici není znak)
    let emptyLetterCounter = 0;
    for (let i = 0; i < wordSearch.grid.length; i++) {
        for (let j = 0; j < wordSearch.grid[i].length; j++) {
            if (!wordSearch.grid[i][j]) {
                emptyLetterCounter++;
            }
        }
    }
    return emptyLetterCounter;
}

function addSolution(wordSearch, count) {
    //přidání dat o tajence + vyplnění nullů v gridu
    const solution = randomWordWithLength(count); //tajenka se vybere náhodně podle počtu zbývajících písmen
    let counter = 0;
    for (let i = 0; i < wordSearch.grid.length; i++) {
        for (let j = 0; j < wordSearch.grid[i].length; j++) {
            if (!wordSearch.grid[i][j]) {
                wordSearch.grid[i][j] = solution[counter++];
            }
        }
    }
    wordSearch.solution = solution.word;
    wordSearch.solutionSpaced = solution.word;
    wordSearch.sentenceBefore = `Toto slovo: [${solution.definition}] se anglicky řekne: `;
    wordSearch.sentenceAfter = '.';
}

function tryUp(wordSearch) {
    const word = wordSearch.words[wordSearch.words.length - 1].word;
    const sRow = wordSearch.words[wordSearch.words.length - 1].sRow;
    const sCol = wordSearch.words[wordSearch.words.length - 1].sCol;
    if (sRow - word.length + 1 >= 0) {
        for (let i = 0; i < word.length; i++) {
            if (
                wordSearch.grid[sRow - i][sCol] &&
                wordSearch.grid[sRow - i][sCol] != word[i]
            ) {
                return false;
            }
        }
        for (let i = 0; i < word.length; i++) {
            wordSearch.grid[sRow - i][sCol] = word[i];
        }
        wordSearch.words[wordSearch.words.length - 1].eRow =
            sRow - word.length + 1;
        wordSearch.words[wordSearch.words.length - 1].eCol = sCol;
        return true;
    }
    return false;
}

function tryRightUp(wordSearch) {
    const word = wordSearch.words[wordSearch.words.length - 1].word;
    const sRow = wordSearch.words[wordSearch.words.length - 1].sRow;
    const sCol = wordSearch.words[wordSearch.words.length - 1].sCol;
    if (
        sCol + word.length < wordSearch.grid[sRow].length &&
        sRow - word.length + 1 >= 0
    ) {
        for (let i = 0, j = 0; i < word.length; i++, j++) {
            if (
                wordSearch.grid[sRow - i][sCol + j] &&
                wordSearch.grid[sRow - i][sCol + j] != word[i]
            ) {
                return false;
            }
        }
        for (let i = 0, j = 0; i < word.length; i++, j++) {
            wordSearch.grid[sRow - i][sCol + j] = word[i];
        }
        wordSearch.words[wordSearch.words.length - 1].eRow =
            sRow - word.length + 1;
        wordSearch.words[wordSearch.words.length - 1].eCol =
            sCol + word.length - 1;
        return true;
    }
    return false;
}

function tryRight(wordSearch) {
    const word = wordSearch.words[wordSearch.words.length - 1].word;
    const sRow = wordSearch.words[wordSearch.words.length - 1].sRow;
    const sCol = wordSearch.words[wordSearch.words.length - 1].sCol;
    if (sCol + word.length < wordSearch.grid[sRow].length) {
        for (let i = 0; i < word.length; i++) {
            if (
                wordSearch.grid[sRow][sCol + i] &&
                wordSearch.grid[sRow][sCol + i] != word[i]
            ) {
                return false;
            }
        }
        for (let i = 0; i < word.length; i++) {
            wordSearch.grid[sRow][sCol + i] = word[i];
        }
        wordSearch.words[wordSearch.words.length - 1].eRow = sRow;
        wordSearch.words[wordSearch.words.length - 1].eCol =
            sCol + word.length - 1;
        return true;
    }
    return false;
}

function tryRightDown(wordSearch) {
    const word = wordSearch.words[wordSearch.words.length - 1].word;
    const sRow = wordSearch.words[wordSearch.words.length - 1].sRow;
    const sCol = wordSearch.words[wordSearch.words.length - 1].sCol;
    if (
        sCol + word.length < wordSearch.grid[sRow].length &&
        sRow + word.length < wordSearch.grid.length
    ) {
        for (let i = 0, j = 0; i < word.length; i++, j++) {
            if (
                wordSearch.grid[sRow + i][sCol + j] &&
                wordSearch.grid[sRow + i][sCol + j] != word[i]
            ) {
                return false;
            }
        }
        for (let i = 0, j = 0; i < word.length; i++, j++) {
            wordSearch.grid[sRow + i][sCol + j] = word[i];
        }
        wordSearch.words[wordSearch.words.length - 1].eRow =
            sRow + word.length - 1;
        wordSearch.words[wordSearch.words.length - 1].eCol =
            sCol + word.length - 1;
        return true;
    }
    return false;
}

function tryDown(wordSearch) {
    const word = wordSearch.words[wordSearch.words.length - 1].word;
    const sRow = wordSearch.words[wordSearch.words.length - 1].sRow;
    const sCol = wordSearch.words[wordSearch.words.length - 1].sCol;
    if (sRow + word.length < wordSearch.grid.length) {
        for (let i = 0; i < word.length; i++) {
            if (
                wordSearch.grid[sRow + i][sCol] &&
                wordSearch.grid[sRow + i][sCol] != word[i]
            ) {
                return false;
            }
        }
        for (let i = 0; i < word.length; i++) {
            wordSearch.grid[sRow + i][sCol] = word[i];
        }
        wordSearch.words[wordSearch.words.length - 1].eRow =
            sRow + word.length - 1;
        wordSearch.words[wordSearch.words.length - 1].eCol = sCol;
        return true;
    }
    return false;
}

function tryLeftDown(wordSearch) {
    const word = wordSearch.words[wordSearch.words.length - 1].word;
    const sRow = wordSearch.words[wordSearch.words.length - 1].sRow;
    const sCol = wordSearch.words[wordSearch.words.length - 1].sCol;
    if (
        sCol - word.length + 1 >= wordSearch.grid[sRow].length &&
        sRow + word.length < wordSearch.grid.length
    ) {
        for (let i = 0, j = 0; i < word.length; i++, j++) {
            if (
                wordSearch.grid[sRow + i][sCol - j] &&
                wordSearch.grid[sRow + i][sCol - j] != word[i]
            ) {
                return false;
            }
        }
        for (let i = 0, j = 0; i < word.length; i++, j++) {
            wordSearch.grid[sRow + i][sCol - j] = word[i];
        }
        wordSearch.words[wordSearch.words.length - 1].eRow =
            sRow + word.length - 1;
        wordSearch.words[wordSearch.words.length - 1].eCol =
            sCol - word.length + 1;
        return true;
    }
    return false;
}

function tryLeft(wordSearch) {
    const word = wordSearch.words[wordSearch.words.length - 1].word;
    const sRow = wordSearch.words[wordSearch.words.length - 1].sRow;
    const sCol = wordSearch.words[wordSearch.words.length - 1].sCol;
    if (sCol - word.length >= wordSearch.grid[sRow].length) {
        for (let i = 0; i < word.length; i++) {
            if (
                wordSearch.grid[sRow][sCol - i] &&
                wordSearch.grid[sRow][sCol - i] != word[i]
            ) {
                return false;
            }
        }
        for (let i = 0; i < word.length; i++) {
            wordSearch.grid[sRow][sCol - i] = word[i];
        }
        wordSearch.words[wordSearch.words.length - 1].eRow = sRow;
        wordSearch.words[wordSearch.words.length - 1].eCol =
            sCol - word.length + 1;
        return true;
    }
    return false;
}

function tryLeftUp(wordSearch) {
    const word = wordSearch.words[wordSearch.words.length - 1].word;
    const sRow = wordSearch.words[wordSearch.words.length - 1].sRow;
    const sCol = wordSearch.words[wordSearch.words.length - 1].sCol;
    if (
        sCol - word.length >= wordSearch.grid[sRow].length &&
        sRow - word.length + 1 >= 0
    ) {
        for (let i = 0, j = 0; i < word.length; i++, j++) {
            if (
                wordSearch.grid[sRow - i][sCol - j] &&
                wordSearch.grid[sRow - i][sCol - j] != word[i]
            ) {
                return false;
            }
        }
        for (let i = 0, j = 0; i < word.length; i++, j++) {
            wordSearch.grid[sRow - i][sCo - j] = word[i];
        }
        wordSearch.words[wordSearch.words.length - 1].eRow =
            sRow - word.length + 1;
        wordSearch.words[wordSearch.words.length - 1].eCol =
            sCol - word.length + 1;
        return true;
    }
    return false;
}

//vrátí slovo, které ještě není v gridu
function newUniqueWord(wordSearch, maxLength) {
    let word;
    while (!word) {
        word = randomWordMinMaxLength(3, maxLength).word;
        for (const el of wordSearch.words) {
            if (el.word == word) {
                word = null;
                break;
            }
        }
    }
    return word;
}

module.exports = { generateWordSearch };

//JENOM TESTOVACÍ DUMMY
// const wordsearch = new WordSearch({
//    words: [
//       {word: "BABRAL", sCol:0, sRow: 8, eCol: 5, eRow: 3},
//       {word: "BIDLO", sCol:4, sRow: 1, eCol: 0, eRow: 5},
//       {word: "BRLOH", sCol:2, sRow: 6, eCol: 2, eRow: 10},
//       {word: "FABIA", sCol:0, sRow: 2, eCol: 4, eRow: 2},
//       {word: "FORMA", sCol:0, sRow: 6, eCol: 4, eRow: 2},
//       {word: "HLODAT", sCol:5, sRow: 6, eCol: 0, eRow: 1},
//       {word: "KARMA", sCol:0, sRow: 7, eCol: 4, eRow: 7},
//       {word: "KOMPROMIS", sCol:3, sRow: 9, eCol: 3, eRow: 1},
//       {word: "KONČETINA", sCol:9, sRow: 8, eCol: 1, eRow: 0},
//       {word: "LILIE", sCol:10, sRow: 0, eCol: 6, eRow: 4},
//       {word: "OCELE", sCol:10, sRow: 4, eCol: 6, eRow: 4},
//       {word: "OČKAŘ", sCol:5, sRow: 5, eCol: 9, eRow: 5},
//       {word: "OPATROVAT", sCol:8, sRow: 0, eCol: 0, eRow: 0},
//       {word: "ORDINACE", sCol:3, sRow: 10, eCol: 10, eRow: 3},
//       {word: "PALMA", sCol:0, sRow: 10, eCol: 4, eRow: 6},
//       {word: "PARABOLY", sCol:1, sRow: 10, eCol: 1, eRow: 3},
//       {word: "PROSIT", sCol:5, sRow: 1, eCol: 10, eRow: 1},
//       {word: "PROTO", sCol:7, sRow: 0, eCol: 3, eRow: 4},
//       {word: "RÁMCE", sCol:6, sRow: 1, eCol: 10, eRow: 5},
//       {word: "ŘEČTINA", sCol:6, sRow: 3, eCol: 6, eRow: 9},
//       {word: "ŘÍMAN", sCol:10, sRow: 10, eCol: 10, eRow: 6},
//       {word: "SONDA", sCol:8, sRow: 8, eCol: 4, eRow: 8},
//       {word: "TENOR", sCol:8, sRow: 6, eCol: 4, eRow: 10},
//       {word: "TESAŘ", sCol:6, sRow: 6, eCol: 10, eRow: 10},
//       {word: "TISÍC", sCol:9, sRow: 0, eCol: 9, eRow: 4},
//       {word: "TLAPA", sCol:0, sRow: 3, eCol: 4, eRow: 7},
//       {word: "VIKÁŘ", sCol:6, sRow: 10, eCol: 10, eRow: 10},
//       {word: "VÝSUN", sCol:6, sRow: 10, eCol: 10, eRow: 6},
//    ],
//    sentenceBefore: "„Příští rok na dovolenou k moři už nepojedu!“ „Proč, je pro vás příliš slané?“",
//    sentenceAfter: ".",
//    solution: "NE,MASTNÉ",
//    solutionSpaced: "Ne, mastné",
//    height: 11,
//    width: 11
// });

// wordsearch.save().then(e => console.log('mmm'));
