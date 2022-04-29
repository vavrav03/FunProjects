module.exports = {
    DB_URL: 'mongodb://127.0.0.1/crosswords_games',
    UNACCEPTABLE_USE_COUNT: 200, //kolikrát je možné načíst křížovku / osmisměrku, než má být vyřazena
    MAX_SOLUTION_LENGTH: 16, //maximální délka tajenky pro křížovky
    MIN_SOLUTION_LENGTH: 10, //minimální délka tajenky pro křížovky
    ENOUGH_CROSSWORDS_GAMES: 100, //požadovaný počet křížovek v databázi
    ENOUGH_WORD_SEARCH_GAMES: 100, //požadovaný počet osmisměrek v databázi
    MIN_WORD_SEARCH_SIZE: 7, //minimální rozměr osmisměrky (výška, šířka)
    MAX_WORD_SEARCH_SIZE: 14, //maximální rozměr osmisměrky
};
