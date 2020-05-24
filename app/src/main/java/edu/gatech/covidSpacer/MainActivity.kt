package edu.gatech.covidSpacer

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanResult
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import br.edu.uepb.nutes.simpleblescanner.SimpleBleScanner
import br.edu.uepb.nutes.simpleblescanner.SimpleScannerCallback
import com.uriio.beacons.Beacons
import com.uriio.beacons.model.iBeacon
import java.util.*
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private val PERMISSION_REQUEST_COARSE_LOCATION = 1
    private val PERMISSION_REQUEST_FINE_LOCATION = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val majorNum = findViewById<TextView>(R.id.majorNum)
        val minorNum = findViewById<TextView>(R.id.minorNum)
        val sw1 = findViewById<Switch>(R.id.broadcastSwitch)

        val mScanner = SimpleBleScanner.Builder()
            .addScanPeriod(15000) // 15s in milliseconds
            .build()

       val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        if (!mBluetoothAdapter.isEnabled) {
            mBluetoothAdapter.enable();
        }

        sw1?.setOnCheckedChangeListener { _, isChecked ->

            //indicate the state of the broadcast
            val message = if (isChecked) "Broadcast: On" else "Broadcast: Off"
            Toast.makeText(this@MainActivity, message,
                Toast.LENGTH_SHORT).show()

            //Enable bluetooth if it isn't already enabled
            if (!mBluetoothAdapter.isEnabled) {
                mBluetoothAdapter.enable();
            }

            mScanner.startScan(object : SimpleScannerCallback {

                override fun onScanResult(callbackType: Int, scanResult: ScanResult) {
                    val device = scanResult.getDevice()
                    val rssi = scanResult.rssi
                    Log.d("MainActivity", "Found Device: " + device.toString() + " Rssi: " + rssi.toString())
                    //Originally I asked to show all the found rssi's
                    //editText.append("Device: " + device.toString() + " rssi: " + rssi.toString() + "\n")
                }

                override fun onBatchScanResults(scanResults: List<ScanResult>) {
                    Log.d("MainActivity", "onBatchScanResults(): " + Arrays.toString(scanResults.toTypedArray()))
                }

                override fun onFinish() {
                    Log.d("MainActivity", "onFinish()")
                }

                override fun onScanFailed(errorCode: Int) {
                    Log.d("MainActivity", "onScanFailed() $errorCode")
                }
            })
        }

        val minor: Int = randomGenerate()
        val major: Int = randomGenerate()

        minorNum.text = minor.toString()
        majorNum.text = major.toString()


        // Make sure we have access coarse location enabled, if not, prompt the user to enable it
        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            val builder =
                AlertDialog.Builder(this)
            builder.setTitle("This app needs location access")
            builder.setMessage("Please grant location access so this app can detect peripherals.")
            builder.setPositiveButton("ok", null)
            builder.setOnDismissListener {
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                    PERMISSION_REQUEST_COARSE_LOCATION
                )
            }
            builder.show()
        }

        // Make sure we have access fine location enabled, if not, prompt the user to enable it
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            val builder =
                AlertDialog.Builder(this)
            builder.setTitle("This app needs location access")
            builder.setMessage("Please grant location access so this app can detect peripherals.")
            builder.setPositiveButton("ok", null)
            builder.setOnDismissListener {
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    PERMISSION_REQUEST_FINE_LOCATION
                )
            }
            builder.show()
        }

        //Initialize beacon and assign a UUID, in my original application the beacon starts when the app starts,
        // now we want it to start when button?


        Beacons.initialize(this)

        // For development we are using the following UUID
        // DDDD98FF-2900-441A-802F-9C398FC1DDDD
        fun byteArrayOfInts(vararg ints: Int) = ByteArray(ints.size) { pos -> ints[pos].toByte() }
        val uuid = byteArrayOfInts(0xDD, 0xDD, 0x98, 0xFF, 0x29, 0x00, 0x44, 0x1A, 0x80, 0x2F, 0x9C, 0x39, 0x8F, 0xC1, 0xDD, 0xDD)
        iBeacon(uuid, major, minor).start() //EXAMPLE

        Log.d("MainActivity", "About to initialize scan")

        // Lets set up the scan



        Log.d("MainActivity", "About to start scan")

        // Originally the scan starts when push button , I understand that now we wanting to start with the app?

        //button.setOnClickListener {
           /*mScanner.startScan(object : SimpleScannerCallback {
                override fun onScanResult(callbackType: Int, scanResult: ScanResult) {
                    val device = scanResult.getDevice()
                    val rssi = scanResult.rssi
                    Log.d("MainActivity", "Found Device: " + device.toString() + " Rssi: " + rssi.toString())
                    //Originally I asked to show all the found rssi's
                    //editText.append("Device: " + device.toString() + " rssi: " + rssi.toString() + "\n")
                }

                override fun onBatchScanResults(scanResults: List<ScanResult>) {
                    Log.d("MainActivity", "onBatchScanResults(): " + Arrays.toString(scanResults.toTypedArray()))
                }

                override fun onFinish() {
                    Log.d("MainActivity", "onFinish()")
                }

                override fun onScanFailed(errorCode: Int) {
                    Log.d("MainActivity", "onScanFailed() $errorCode")
                }
            })*/
        //}
    }

    private fun randomGenerate(): Int {

        val bound = 65536
        //generate random values from 0-65536
        return Random(System.nanoTime()).nextInt(bound)
    }


    //lets check what happened with the permissions requested...
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        when (requestCode) {
            PERMISSION_REQUEST_COARSE_LOCATION -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    println("coarse location permission granted")
                } else {
                    val builder: AlertDialog.Builder = AlertDialog.Builder(this)
                    builder.setTitle("Functionality limited")
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.")
                    builder.setPositiveButton("OK", null)
                    builder.show()
                }
                return
            }
            PERMISSION_REQUEST_FINE_LOCATION -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    println("fine location permission granted")
                } else {
                    val builder: AlertDialog.Builder = AlertDialog.Builder(this)
                    builder.setTitle("Functionality limited")
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.")
                    builder.setPositiveButton("OK", null)
                    builder.show()
                }
                return
            }
        }
    }


}
