class Field {
    #field
    #amountOfCellsX
    #amountOfCellsY

    constructor(xMax, yMax) {
        this.#amountOfCellsX = xMax
        this.#amountOfCellsY = yMax
        this.fillArray()
    }

    getField() {
        return this.#field
    }

    fillArray() {
        this.#field = []
        for (let i = 0; i <= this.#amountOfCellsX * 2 + 1; i++) {
            this.#field[i] = []
        }
    }

    create(obj, x, y) {
        this.#field[x + this.#amountOfCellsX][-y + this.#amountOfCellsY] = obj
    }

    read(x, y) {
        return this.#field[x + this.#amountOfCellsX][-y + this.#amountOfCellsY]
    }

    update(obj, x, y) {
        this.#field[x + this.#amountOfCellsX][-y + this.#amountOfCellsY] = obj
    }

    delete(x, y) {
        this.#field[x + this.#amountOfCellsX][-y + this.#amountOfCellsY] = null
    }
}