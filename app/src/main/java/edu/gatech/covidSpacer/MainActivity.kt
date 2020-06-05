package edu.gatech.covidSpacer

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.RemoteException
import android.util.Log
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.uriio.beacons.Beacons
import com.uriio.beacons.model.iBeacon
import org.altbeacon.beacon.*
import kotlin.random.Random

class MainActivity : AppCompatActivity(), BeaconConsumer {

    private val PERMISSION_REQUEST_COARSE_LOCATION = 1
    private val PERMISSION_REQUEST_FINE_LOCATION = 2
    private val COVID_SPACER_UUID = "DDDD98FF-2900-441A-802F-9C398FC1DDDD"
    private var beaconManager: BeaconManager? = null
    private var isScanning = true


    data class BeaconDataClass(val majorVal:String, val minorVal: String, var rssiArray: IntArray = IntArray(3){0}, var distanceArray: DoubleArray = DoubleArray(3){10.0}, var avgRssi:Int = 0, var avgDistance:Double = 10.0)

    var idMap = mutableMapOf<Beacon, BeaconDataClass>()

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val majorNum = findViewById<TextView>(R.id.majorNum)
        val minorNum = findViewById<TextView>(R.id.minorNum)

        //reference switch to be manipulated
        val sw1 = findViewById<Switch>(R.id.broadcastSwitch)



        //set the text view to be changed by the scan results


        //set map to store scan results




        //val mScanner = SimpleBleScanner.Builder()
            //.addScanPeriod(15000)
            //.addFilterServiceUuid("DDDD98FF-2900-441A-802F-9C398FC1DDDD")// 15s in milliseconds
           // .build()

        //connect to bluetooth module on phone
       val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        Log.d("bluetoothinfo", mBluetoothAdapter.address.toString())

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

        val region = Region(
            "myBeacons",
            Identifier.parse("DDDD98FF-2900-441A-802F-9C398FC1DDDD"),
            Identifier.parse("2185"),
            Identifier.parse("1063")
        )
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

                isScanning = true

                broadcastBeacon.start()
                beaconManager = BeaconManager.getInstanceForApplication(this)
                beaconManager!!.getBeaconParsers().add(
                    BeaconParser().
                    setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
                beaconManager!!.startMonitoringBeaconsInRegion(region);
                beaconManager!!.bind(this)



            }
            else {
                isScanning = false
                broadcastBeacon.stop()
                //mScanner.stopScan()
                val deviceList = findViewById<TextView>(R.id.deviceList)
                deviceList.setText("No Devices Found")
                /*beaconManager!!.stopMonitoringBeaconsInRegion(Region(
                    "myRangingUniqueId",
                    null,
                    null,
                    null
                ))*/
                beaconManager!!.unbind(this)

                idMap = mutableMapOf<Beacon, BeaconDataClass>()


            }
        }
        Log.d("MainActivity", "About to initialize scan")

        // Lets set up the scan



        Log.d("MainActivity", "About to start scan")

        // Originally the scan starts when push button , I understand that now we wanting to start with the app?


    }
    override fun onDestroy() {
        super.onDestroy()
        beaconManager!!.unbind(this)
    }
    override fun onBeaconServiceConnect() {

            Log.d("BeaconService", "Entered service connect")

            val deviceList = findViewById<TextView>(R.id.deviceList)

            Log.d("BeaconService", beaconManager.toString())
            beaconManager!!.addRangeNotifier(object : RangeNotifier {
                override fun didRangeBeaconsInRegion(beacons: Collection<Beacon>, region: Region) {
                    Log.d("BEACONDS", beacons.toString())
                    if (beacons.size > 0) {

                        for (bcn in beacons) {
                            if (bcn.id1.toString().equals(COVID_SPACER_UUID, true))
                            //idMap[bcn] = (bcn.distance*3.2808399).toString()
                                if(idMap.keys.contains(bcn)) {
                                    val  rssiTemp = idMap[bcn]!!.rssiArray
                                    rssiTemp[0] = rssiTemp[1]
                                    rssiTemp[1] = rssiTemp[2]
                                    rssiTemp[2] = bcn.rssi
                                    val distanceTemp = idMap[bcn]!!.distanceArray
                                    distanceTemp[0] = distanceTemp[1]
                                    distanceTemp[1] = distanceTemp[2]
                                    distanceTemp[2] = bcn.distance
                                    idMap[bcn] = idMap[bcn]!!.copy(rssiArray = rssiTemp, distanceArray = distanceTemp, avgRssi = rssiTemp.average().toInt(), avgDistance = distanceTemp.average())
                                                                        }
                                else {
                                    idMap[bcn] = BeaconDataClass(majorVal = bcn.id2.toString(),
                                        minorVal = bcn.id3.toString(),
                                        rssiArray = IntArray(3) { bcn.rssi },
                                        distanceArray = DoubleArray(3) { bcn.distance },
                                        avgRssi = bcn.rssi, avgDistance = bcn.distance)
                                }

                        }


                        val tempMap = idMap
                        val iter = tempMap.iterator()
                        while (iter.hasNext()) {
                            val keyval = iter.next()
                            if (keyval.key !in beacons)
                                idMap.remove(keyval.key)
                        }

                        deviceList.text = ""
                        for (k in idMap.keys) {
                            val bcnData = idMap[k]
                            deviceList.append("Device: " + k.bluetoothAddress.toString() + "Avg. Rssi:" + bcnData!!.avgRssi + "\n")
                        }


                        val firstBeacon = beacons.iterator().next()
                        runOnUiThread {

                            Log.d("FIRSTBEACONS", firstBeacon.id1.toString())
                            Log.d(
                                "beacon",
                                "The coffee beacon " + firstBeacon.toString() + " is about " + firstBeacon.distance + " meters away."
                            )
                        }
                    }
                }
            })

            try {
                beaconManager!!.startRangingBeaconsInRegion(
                    Region(
                        "myRangingUniqueId",
                        null,
                        null,
                        null
                    )
                )
            } catch (e: RemoteException) {
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


}
