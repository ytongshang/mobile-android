package cradle.rancune.apm.memory

import androidx.annotation.Keep

/**
 * Created by Rancune@126.com 3/9/21.
 */
@Keep
data class Memory(
  var heapMaxMemory: Double = 0.0,
  var heapTotalMemory: Double = 0.0,
  var heapFreeMemory: Double = 0.0,
  var heapProportion: Double = 0.0
)