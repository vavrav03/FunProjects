const Crosswords = require('../models/Crosswords');

const { randomWord, randomSolution } = require('./DictionaryReader');

function generateCrosswords() {
    const solutionWordCouple = randomSolution(); //náhodný výběr tajenky
    const solutionWord = solutionWordCouple.word;
    const solutionDefinition = solutionWordCouple.definition;
    const crosswords = {
        parts: [],
        sentenceBefore: `Toto slovo: [${solutionDefinition}] se anglicky řekne: `,
        sentenceAfter: '.',
        solutionSpaced: solutionWord,
    };
    let row = 0;
    while (row != solutionWord.length) {
        //náhodný výběr slov tak, aby slovo obsahovalo písmeno tajenky v daném řádku
        const wordCouple = newUniqueWord(crosswords.parts);
        const solutionIndex = wordCouple.word.indexOf(solutionWord[row]);
        if (solutionIndex != -1) {
            crosswords.parts.push({
                word: wordCouple.word,
                definition: wordCouple.definition,
                solutionIndex: solutionIndex,
            });
            row++;
        }
    }
    return new Crosswords(crosswords);
}

//vrátí slovo, které ještě není v gridu
function newUniqueWord(parts) {
    let wordCouple;
    while (!wordCouple) {
      wordCouple = randomWord();
        for (const el of parts) {
            if (el.word == wordCouple.word) {
               wordCouple = null;
                break;
            }
        }
    }
    return wordCouple;
}

module.exports = { generateCrosswords };

//DUMMY!
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
// const crosswords = new Crosswords({
//    parts: [
//       {
//          word: 'PÍSEŇ',
//          definition: 'První slovo názvu knižní série, na jejímž základě vznikl seriál Hra o trůny.',
//          solutionIndex: 2
//       },
//       {
//          word: 'DAN',
//          definition: 'Křestní jméno autora, k němuž je přirovnávána Jessica Cornwell.',
//          solutionIndex: 0
//       },
//       {
//          word: 'FILIPÍNY',
//          definition: 'Jméno ostrovního státu, kde se odehrává děj románu Daleko od Nifelheimu.',
//          solutionIndex: 5
//       },
//       {
//          word: 'HELEN',
//          definition: 'Jméno hlavní hrdinky knihy Bohyně.',
//          solutionIndex: 2
//       },
//       {
//          word: 'DUDEK',
//          definition: 'Příjmení ilustrátora knihy Cestovatelské pohádky.',
//          solutionIndex: 3
//       },
//       {
//          word:  'TROJICE',
//          definition: 'Poslední slovo názvu televizní série, jejímž spolutvůrcem je Michal Sýkora.',
//          solutionIndex: 3
//       },
//       {
//          word: 'ANTONÍNA',
//          definition: 'Jméno svobodné matky, vystupující v knize Čas žen, která se přistěhovala do bytu se svou dcerou. ',
//          solutionIndex: 4
//       },
//       {
//          word: 'FRANK',
//          definition: 'Křestní jméno nájemného zabijáka vystupujícího v knize Jak se loučí střelec.',
//          solutionIndex: 2
//       },
//       {
//          word: 'FISCHER',
//          definition: 'Příjmení moderátora diskuzního večera – křtu knihy Evropa, Rusko, teroristé a běženci.',
//          solutionIndex: 0
//       },
//       {
//          word: 'HRABAL',
//          definition: 'Příjmení neoblíbenějšího českého spisovatele islandského básníka, spisovatele a textaře Sjóna.',
//          solutionIndex: 3
//       },
//       {
//          word: 'CURRAN',
//          definition: 'Křestní jméno bývalého Pána šelem vystupujícího v knize Magie změny.',
//          solutionIndex: 4
//       },
//       {
//          word: 'ŽIVOTA',
//          definition: 'Druhá část názvu sborníku článků, esejů a glos Jana Sokola.',
//          solutionIndex: 2
//       },
//       {
//          word: 'GRAYSON',
//          definition: 'Křestní jméno syna miliardáře, jenž je jedním z pěti hlavních protagonistů knihy Let 305. ',
//          solutionIndex:3
//       },
//       {
//          word: 'JOHAN',
//          definition: 'Křestní jméno medika – jednoho z hrdinů knihy Serpens Levis.',
//          solutionIndex: 2
//       },
//       {
//          word: 'FAIRFAXOVÁ',
//          definition: 'Jméno hospodyně vyprávějící příběh Jany Eyrové v knize Kdo pohne osudem.',
//          solutionIndex: 3
//       },
//       {
//          word: 'DANIEL',
//          definition: 'Křestní jméno pseudonymu, pod nímž byl v roce 2008 vydán román Ochlazení. ',
//          solutionIndex: 1
//       },
//       {
//          word: 'JENNY',
//          definition: 'Jméno jedné z postav knihy Láska s chutí makronky – baculaté ženy řešící problémy v manželství.',
//          solutionIndex: 0
//       },
//    ],
//    sentenceBefore: '„Měl bys udělat tohle: “',
//    sentenceAfter: '.',
//    solutionSpaced: 'Sdílej na FB a vyhraj',
// });

// crosswords.save().then(e => console.log('fff'));
