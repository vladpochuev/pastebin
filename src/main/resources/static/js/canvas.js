const createCanvas = (id) => {
    return new fabric.Canvas(id, {
        width: window.innerWidth,
        height: window.innerHeight,
        selection: false,
        hoverCursor: 'grab',
        moveCursor:'grabbing',
        backgroundColor: '#FFE4DE',
        fireRightClick: true,
        stopContextMenu: true,
    });
}

const initCanvas = () => {
    canvas.renderAll()
    setPanEvents(canvas)
    createGrid(clusterSizeX, clusterSizeY)
    getIntoCenter()
}

const setPanEvents = (canvas) => {
    canvas.on('mouse:move', e => {
        const point = getExactPoint(e)
        const coords = document.querySelector(".current-coords")
        coords.textContent = `(${point.x};${-point.y})`

        if (mousePressed && e.button === 1) {
            canvas.setCursor('grab')
            shiftCanvas(e.e.movementX, e.e.movementY)
        }
    })

    canvas.on('mouse:down', e => {
        if(e.button === 1) {
            mousePressed = true
            canvas.setCursor('grab')
        } else if(e.button === 3 && e.target == null) {
            mousePressed = false
            let point = getExactPoint(e)
            window.lastClickX = point.x
            window.lastClickY = point.y
            openPopup('popup-create-bg')
        }
    })

    canvas.on('mouse:up', e => {
        if(e.button === 1) {
            mousePressed = false
            canvas.setCursor('default')
        }
    })

    canvas.on('mouse:wheel', opt => {
        let delta = opt.e.deltaY
        zoom(delta, opt.e.offsetX, opt.e.offsetY)
        opt.e.preventDefault()
        opt.e.stopPropagation()
    })
}

const restrictBorders = (newLeft, newTop, zoom) => {
    if (newLeft < -clusterSizeX * 100) {
        camera.left = -clusterSizeX * 100
    } else if (newLeft + camera.width / zoom > clusterSizeX * 101) {
        camera.left = clusterSizeX * 101 - camera.width / zoom
    } else {
        camera.left = newLeft
    }

    if (newTop < -clusterSizeY * 100) {
        camera.top = -clusterSizeY * 100
    } else if (newTop + camera.height / zoom > clusterSizeY * 101) {
        camera.top = clusterSizeY * 101 - camera.height / zoom
    } else {
        camera.top = newTop
    }
}

const moveInto = (newLeft, newTop, zoom) => {
    restrictBorders(newLeft, newTop, zoom)

    canvas.viewportTransform[4] = -camera.left * zoom;
    canvas.viewportTransform[5] = -camera.top * zoom;

    canvas.relativePan(new fabric.Point(0, 0))
}

const zoom = (delta, offsetX, offsetY) => {
    let zoom = canvas.getZoom();
    zoom *= 0.999 ** delta

    if (zoom > 3) zoom = 3
    if (zoom < 0.15) zoom = 0.15

    setZoomTitle(zoom)
    canvas.zoomToPoint({ x: offsetX, y: offsetY }, zoom)

    const newLeft = -canvas.viewportTransform[4] / zoom;
    const newTop = -canvas.viewportTransform[5] / zoom;
    moveInto(newLeft, newTop, zoom)

    canvas.requestRenderAll()
}

const setZoomTitle = (zoom) => {
    const zoomLevel = document.querySelector('#zoom-level')
    zoomLevel.textContent = Math.ceil(zoom * 100) + "%"
}

const getPointer = (e) => {
    let touch = e.e.touches ? e.e.touches[0] : e.e
    const x = canvas.getPointer(touch).x
    const y = canvas.getPointer(touch).y
    return new fabric.Point(x, y)
}

const getExactPoint = (e) => {
    const pointer = getPointer(e)
    const x = Math.floor(pointer.x/clusterSizeX)
    const y = Math.floor(pointer.y/clusterSizeY)
    return new fabric.Point(x,y)
}

const createObject = (id, title, x, y) => {
    const rect = new fabric.Rect({
        width: clusterSizeX,
        height: clusterSizeY,
        left: clusterSizeX/2 + (x * clusterSizeX),
        top: clusterSizeY/2 + -(y * clusterSizeY),
        fill: 'green',
        originX: 'center',
        originY: 'center',
    })

    const trim = (string) => {
        const maxSize = clusterSizeX
        const ctx = canvas.getContext()
        if (ctx.measureText(string).width * (clusterSizeX / 56) <= maxSize) {
            return string
        } else {
            return findMaxLength(string, maxSize, ctx)
        }
    }

    const findMaxLength = (string, maxSize, ctx) => {
        let len = ''
        for (let i = 0;ctx.measureText(len + '...').width * (clusterSizeX / 56) <= maxSize; i++) {
            len = string.substring(0, i)
        }
        return len + '...'
    }

    let text = trim(title) + '\n' +
        trim(`(${x};${y})`) + '\n' +
        trim(id)

    let textBox = new fabric.Textbox(text, {
        originX: 'center', originY: 'center',
        left: clusterSizeX/2 + (x * clusterSizeX),
        top: clusterSizeY/2 + -(y * clusterSizeY),
        height: Math.floor(clusterSizeX),
        width: Math.floor(clusterSizeY),
        textAlign: 'center',
        fontSize: clusterSizeY / 7
    })

    let group = new fabric.Group([rect, textBox], {hasControls: false,
        hasBorders: false,
        lockMovementX: true,
        name: id,
        lockMovementY: true})

    group.on('mousemove', () => {
        if(!mousePressed) {
            canvas.setCursor('default')
        }
    })

    group.on('mousedown', e => {
        if(e.button === 3) {
            getAndShowBin(e.target.name)
        }
    })

    field.create(group, x, y)
    canvas.add(group)
}

const createGrid = (x, y) => {
    const properties = {stroke: '#c5897c', evented: false, lockMovementX: true, lockMovementY: true}
    for (let i = -amountOfCellsX; i <= amountOfCellsX + 1; i++) {
        canvas.add(new fabric.Line([i * x, (amountOfCellsY + 1) * y, i * x, amountOfCellsY * -y], properties))
    }
    for (let i = -amountOfCellsY; i <= amountOfCellsY + 1; i++) {
        canvas.add(new fabric.Line([(amountOfCellsX + 1) * x, i * y, amountOfCellsX * -x, i * y], properties))
    }
}

const shiftCanvas = (x, y) => {
    const zoom = canvas.getZoom()
    const newLeft = camera.left - x / zoom
    const newTop = camera.top - y / zoom

    moveInto(newLeft, newTop, zoom)

    canvas.requestRenderAll();
}

const getIntoCenter = () => {
    camera.left = -clusterSizeX * 3
    camera.top = -clusterSizeY
    canvas.viewportTransform[4] = -clusterSizeX * 3
    canvas.viewportTransform[5] = -clusterSizeY
    canvas.absolutePan(new fabric.Point(-clusterSizeX * 3, -clusterSizeY))
}

const resizeCanvas = () => {
    canvas.setHeight(window.innerHeight);
    canvas.setWidth(window.innerWidth);
    canvas.renderAll();
}

window.addEventListener('resize', resizeCanvas, false);