package nl.nicolaielectronics.nlock

import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import android.util.Log

class HostCardEmulatorService: HostApduService() {
    companion object {
        val TAG = "Host Card Emulator"
        val STATUS_SUCCESS = "9000"
        val STATUS_FAILED = "6F00"
        val CLA_NOT_SUPPORTED = "6E00"
        val INS_NOT_SUPPORTED = "6D00"
        val AID = "42000000000001"
        val SELECT_INS = "A4"
        val AUTH1_INS = "86"
        val AUTH2_INS = "87"
        val DEFAULT_CLA = "00"
        val MIN_APDU_LENGTH = 12
    }

    override fun onDeactivated(reason: Int) {
        Log.d(TAG, "Deactivated: $reason")
    }

    override fun processCommandApdu(commandApdu: ByteArray?,
                                    extras: Bundle?): ByteArray {
        if (commandApdu == null) {
            Log.d(TAG, "commandApdu is null: failed")
            return Utils.hexStringToByteArray(STATUS_FAILED)
        }

        val hexCommandApdu = Utils.toHex(commandApdu)
        if (hexCommandApdu.length < MIN_APDU_LENGTH) {
            Log.d(TAG, "hexCommandApdu is $hexCommandApdu, too short")
            return Utils.hexStringToByteArray(STATUS_FAILED)
        }

        Log.d(TAG, "hexCommandApdu is $hexCommandApdu")

        if (hexCommandApdu.substring(0, 2) != DEFAULT_CLA) {
            Log.d(TAG, "commandApdu is not default CLA")
            return Utils.hexStringToByteArray(CLA_NOT_SUPPORTED)
        }

        if (hexCommandApdu.substring(2, 4) == SELECT_INS) {
            if (hexCommandApdu.substring(10, 24) == AID) {
                Log.d(TAG, "commandApdu is correct AID " + hexCommandApdu.substring(10, 24))
                val test = "Hello world".toByteArray()
                return test + Utils.hexStringToByteArray(STATUS_SUCCESS)
            } else {
                Log.d(TAG, "commandApdu is wrong AID " + hexCommandApdu.substring(10, 24))
                return Utils.hexStringToByteArray(STATUS_FAILED)
            }
        } else if (hexCommandApdu.substring(2, 4) == AUTH1_INS) {
            Log.d(TAG, "authenticate step 1: identify user")
            val test = "nl.tkkrlab.renze".toByteArray()
            return test + Utils.hexStringToByteArray(STATUS_SUCCESS)
        } else if (hexCommandApdu.substring(2, 4) == AUTH2_INS) {
            Log.d(TAG, "authenticate step 2: challenge")
            val test = "dummyResponse".toByteArray()
            return test + Utils.hexStringToByteArray(STATUS_SUCCESS)
        } else {
            Log.d(TAG, "commandApdu is not supported" + hexCommandApdu.substring(2, 4))
            return Utils.hexStringToByteArray(INS_NOT_SUPPORTED)
        }
    }
}
