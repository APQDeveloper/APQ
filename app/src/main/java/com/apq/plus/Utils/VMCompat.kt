package com.apq.plus.Utils

import android.graphics.Bitmap
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.math.BigDecimal

/**
 * Created by zhufu on 3/17/18.
 */
object VMCompat {
    fun getVMProfileByJSON(profile: String?): VMProfile?{
        if (profile.isNullOrEmpty()){
            return null
        }
        val gson = Gson()
        return try {
            gson.fromJson(profile, VMProfile::class.java)
        }catch (e: Exception){
            e.printStackTrace()
            null
        }
    }

    fun getBaseInfo(json: String): BaseInfo?{
        return try {
            val obj = JSONObject(json)
            BaseInfo(obj.getString("name"),obj.getString("description"),null,obj.getString("id").toInt())
        }catch (e: Exception){
            e.printStackTrace()
            null
        }
    }

    fun getBaseInfo(file: File): BaseInfo{
        val result: BaseInfo? = getBaseInfo(file.readText())
        if (result != null){
            result.file = file.path
            return result
        }
        else{
            val tmp = BaseInfo("","",null,-1)
            tmp.file = file.path
            return tmp
        }
    }

    class BaseInfo(val name: String,val description: String,val icon: Bitmap?,val id: Int){
        var file: String? = null
        val profile: VMProfile?
        get() = if (file != null) getVMProfileByJSON(File(file).readText()) else null
        val isNull: Boolean
        get() = id == -1 || (name.isEmpty() && description.isEmpty())

        val useVNC: Boolean
        get() = try {
            JSONObject(File(file).readText())["useVnc"].toString().toBoolean()
        }catch (e: Throwable){
            e.printStackTrace()
            false
        }
        val diskHolder: VMProfile.DiskHolder?
        get() {
            if (file == null) return null
            val text: String?
            try {
                text = JSONObject(File(file).readText())["disks"].toString()
            }catch (e: JSONException){
                e.printStackTrace()
                return null
            }

            return Gson().fromJson(text,VMProfile.DiskHolder::class.java)
        }
        val totalSize: BigDecimal
        get() {
            var result = 0.toBigDecimal()
            diskHolder?.forEach { result += FileUtils.getFileSize(it.diskFile!!,FileUtils.FileSizeUnits.MB) }
            return result
        }

        val monitorPort: Int
        get() = if (isNull) -1 else id + 4444

        val videoPort: Int
        get() = if (isNull) -1 else if (useVNC) id else 0

        override fun equals(other: Any?): Boolean {
            return other is BaseInfo && (other.id==id && other.name == name)
        }

        override fun hashCode(): Int {
            var result = name.hashCode()
            result = 31 * result + description.hashCode()
            result = 31 * result + (icon?.hashCode() ?: 0)
            result = 31 * result + id
            result = 31 * result + (file?.hashCode() ?: 0)
            return result
        }
    }
}