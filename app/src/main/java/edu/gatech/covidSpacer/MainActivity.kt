package edu.gatech.covidSpacer

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanResult
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
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

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val majorNum = findViewById<TextView>(R.id.majorNum)
        val minorNum = findViewById<TextView>(R.id.minorNum)

        //reference switch to be manipulated
        val sw1 = findViewById<Switch>(R.id.broadcastSwitch)
        val btn1 = findViewById<Button>(R.id.shareButton)


        //set the text view to be changed by the scan results
        val deviceList = findViewById<TextView>(R.id.deviceList)

        //set map to store scan results
        var idMap = mutableMapOf<String, String>()


        val mScanner = SimpleBleScanner.Builder()
            .addScanPeriod(15000)
            //.addFilterServiceUuid("DDDD98FF-2900-441A-802F-9C398FC1DDDD")// 15s in milliseconds
            .build()

        //connect to bluetooth module on phone
       val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        //turn on bluetooth if not enabled
        if (!mBluetoothAdapter.isEnabled) {
            mBluetoothAdapter.enable();
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
        val broadcastBeacon = iBeacon(uuid, major, minor) //EXAMPLE
        //broadcastBeacon.pause() //EXAMPLE

        //set the switch on change listener to toggle the broadcast and scan
        sw1?.setOnCheckedChangeListener { _, isChecked ->

            //indicate the state of the broadcast
            val message = if (isChecked) "Broadcast: On" else "Broadcast: Off"
            Toast.makeText(this@MainActivity, message,
                Toast.LENGTH_SHORT).show()

            if (isChecked) {
                //Enable bluetooth if it isn't already enabled
                if (!mBluetoothAdapter.isEnabled) {
                    mBluetoothAdapter.enable();
                }

                broadcastBeacon.start()

                mScanner.startScan(object : SimpleScannerCallback {

                    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
                    override fun onScanResult(callbackType: Int, scanResult: ScanResult) {
                        val device = scanResult.getDevice()
                        val uuids = device.uuids
                        if(uuids != null)
                            Log.d("UUID message", uuids.toString())
                        else
                            Log.d("UUID", "Null")
                        val rssi = scanResult.rssi
                        Log.d(
                            "MainActivity",
                            "Found Device: " + device.toString() + " Rssi: " + rssi.toString()
                        )
                        //Originally I asked to show all the found rssi's

                        //update map with rssi val and print the list
                        idMap[device.toString()] = rssi.toString()
                        deviceList.text = ""
                        for (k in idMap.keys){
                            deviceList.append("Device: " + k + " rssi: " + idMap[k] + "\n")
                        }

                    }

                    override fun onBatchScanResults(scanResults: List<ScanResult>) {
                        Log.d(
                            "MainActivity",
                            "onBatchScanResults(): " + Arrays.toString(scanResults.toTypedArray())
                        )
                    }

                    override fun onFinish() {
                        Log.d("MainActivity", "onFinish()")
                    }

                    override fun onScanFailed(errorCode: Int) {
                        Log.d("MainActivity", "onScanFailed() $errorCode")
                    }
                })
            }
            else {
                broadcastBeacon.stop()
                mScanner.stopScan()
                deviceList.setText("No Devices Found")
                idMap = mutableMapOf<String, String>()

            }
        }
        Log.d("MainActivity", "About to initialize scan")

        // Lets set up the scan


        Log.d("MainActivity", "About to start scan")

        // Originally the scan starts when push button , I understand that now we wanting to start with the app?


        btn1?.setOnClickListener {
            sendEmail()
        }
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

    private fun sendEmail() {
        val filename = "COVID_Spacer_Data.CVS"
        //val filelocation =
        //File(Environment.getExternalStorageDirectory().getAbsolutePath(), filename)
        //val path = Uri.fromFile(filelocation)
        val emailIntent = Intent(Intent.ACTION_SEND)
        // set the type to 'email'
        emailIntent.type = "vnd.android.cursor.dir/email"

       // val to = arrayOf("ghkurfess@gmail.com")
       // emailIntent.putExtra(Intent.EXTRA_EMAIL, to)
        // the attachment
        //emailIntent.putExtra(Intent.EXTRA_STREAM, path)
        // the mail subject
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "COVID Spacer Data")
        startActivity(Intent.createChooser(emailIntent, "Send email..."))
    }



}
