package cradle.rancune.media

/**
 * Created by Rancune@126.com 2020/3/22.
 */
interface OnDataListener {
    fun onOutputAvailable(data: EncodedData)
}