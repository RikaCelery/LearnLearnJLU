package util


//贝塞尔曲线
fun calculateY(t: Float): Float {
    val tSquared = t * t
    val oneMinusT = 1 - t
    val oneMinusTSquared = oneMinusT * oneMinusT

    val y = oneMinusTSquared * 0.0 + 2 * oneMinusT * t * 0.7 + tSquared * 0.7

    return y.toFloat()
}