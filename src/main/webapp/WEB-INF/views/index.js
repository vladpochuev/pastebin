const initCanvas = (id) => {
    return new fabric.Canvas(id, {
        width: window.innerWidth,
        height: window.innerHeight,
        selection: false,
        hoverCursor: 'grab',
        moveCursor:'grabbing',
        backgroundColor: '#FFE4DE',
        fireRightClick: true,
        stopContextMenu: true
    });
}

const setPanEvents = (canvas) => {
    canvas.on('mouse:move', e => {
        if(mousePressed && e.button === 1 && onField(e)) {
                canvas.setCursor('grab')
                let delta = new fabric.Point(e.e.movementX, e.e.movementY);
                canvas.relativePan(delta)
        }
    })

    canvas.on('mouse:down', e => {
        if(e.button === 1 && onField(e)) {
            mousePressed = true;
            canvas.setCursor('grab')
        } else if(e.button === 3 && e.target == null) {
            const point = getPointer(e)
            createObject(point.x, point.y)
        }
    })

    canvas.on('mouse:up', e => {
        if(e.button === 1) {
            mousePressed = false;
            canvas.setCursor('default')
        }
    })

    canvas.on('mouse:wheel', function(opt) {
        let delta = opt.e.deltaY;
        let zoom = canvas.getZoom();

        zoom *= 0.999 ** delta;

        console.log(Math.round(zoom * 100))
        
        if (zoom > 3) zoom = 3;
        if (zoom < 0.05) zoom = 0.05;
        canvas.zoomToPoint({ x: opt.e.offsetX, y: opt.e.offsetY }, zoom);
        opt.e.preventDefault();
        opt.e.stopPropagation();
    });

}

const getPointer = (e) => {
    let touch = e.e.touches ? e.e.touches[0] : e.e;
    const x = canvas.getPointer(touch).x
    const y = canvas.getPointer(touch).y
    return new fabric.Point(x, y)
}

const onField = (e) => {
    const point = getPointer(e)
    return point.x > -clusterSizeX * 993
    && point.x < clusterSizeX * 993
    && point.y > -clusterSizeY * 993
    && point.y < clusterSizeY * 993
}

const createObject = (x, y) => {
    const originX = Math.floor(x/clusterSizeX)
    const originY = Math.floor(y/clusterSizeY)
    
    const rect = new fabric.Rect({
        width: clusterSizeX,
        height: clusterSizeY,
        left: clusterSizeX/2 + (originX * clusterSizeX),
        top: clusterSizeY/2 + (originY * clusterSizeY),
        fill: 'green',
        originX: 'center',
        originY: 'center',
    })

    let text = new fabric.Text(
        `(${originX};${originY})`, {
            originX: 'center', originY: 'center',
            left: clusterSizeX/2 + (originX * clusterSizeX), top: clusterSizeY/2 + (originY * clusterSizeY),
        })

    let group = new fabric.Group([rect, text], {hasControls: false,
        hasBorders: false,
        lockMovementX: true,
        lockMovementY: true})

    group.on('mousedown', e => {
        if(e.button === 1) console.log('left click')
        else if(e.button === 3) console.log('right click')
    })

    group.on('mousemove', e => {
        canvas.setCursor('default')
    })
    
    canvas.add(group)
}


const canvas = initCanvas('canvas');
let mousePressed = false;
let clusterSizeX = Math.floor(window.innerWidth / 7)
let clusterSizeY = Math.floor(window.innerWidth / 7)

canvas.renderAll()
setPanEvents(canvas)

const createGrid = (x, y) => {
    const properties = {stroke: '#c5897c', evented: false, lockMovementX: true, lockMovementY: true}
    for (let i = -1000; i <= 1000; i++) {
        canvas.add(new fabric.Line([i * x, 1000 * y, i * x, 1000 * -y], properties))
    }
    for (let i = -1000; i <= 1000; i++) {
        canvas.add(new fabric.Line([1000 * x, i * y, 1000 * -x, i * y], properties))
    }
}

createGrid(clusterSizeX, clusterSizeY)

canvas.relativePan(new fabric.Point(clusterSizeX * 3, clusterSizeY))