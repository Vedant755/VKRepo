package com.ftg.carrepo.Screens

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.ftg.carrepo.Models.UserDetails
import com.ftg.carrepo.R
import com.ftg.carrepo.Utils.SharedPrefManager
import com.ftg.carrepo.databinding.FragmentCustomButtonDetailBinding
import com.ftg.carrepo.databinding.FragmentUserPlanChangePageBinding
import javax.inject.Inject


class CustomButtonDetail : Fragment() {
    @Inject
    lateinit var memory: SharedPrefManager
    private lateinit var bind: FragmentCustomButtonDetailBinding

    private lateinit var buttonName: String
    private lateinit var loanNo: String
    private lateinit var custName: String
    private lateinit var vehicleNo: String
    private lateinit var modelNo: String
    private lateinit var chassisNo: String
    private lateinit var branch: String
    private lateinit var level1: String
    private lateinit var level2: String
    private lateinit var level3: String
    private lateinit var level4: String
    private lateinit var contactPerson1: String
    private lateinit var contactPerson2: String
    private lateinit var contactPerson3: String
    private lateinit var vehicleAddress: String
    private lateinit var carries_goods: String
    private lateinit var name:String
    var mobile:Long = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        bind = FragmentCustomButtonDetailBinding.inflate(inflater, container, false)
        return bind.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        extractArguments()
        setUserDetails()
        setOnclickListeners()

    }

    private fun extractArguments() {
        val args = arguments ?: return
        buttonName = args.getString("buttonName", "")
        loanNo = args.getString("loan_no", "-")
        custName = args.getString("cust_name", "-")
        vehicleNo = args.getString("vehicle_no", "-")
        modelNo = args.getString("model_no", "-")
        chassisNo = args.getString("chassis_no", "-")
        branch = args.getString("branch", "-")
        level1 = args.getString("level1", "-")
        level2 = args.getString("level2", "-")
        level3 = args.getString("level3", "-")
        level4 = args.getString("level4", "-")
        carries_goods = bind.carriesGoods.text.toString()
        vehicleAddress = bind.vehicleAddress.text.toString()
        contactPerson1 = args.getString("contact_person1", "-")
        contactPerson2 = args.getString("contact_person2", "-")
        contactPerson3 = args.getString("contact_person3", "-")

        name= args.getString("name","-")
        mobile=args.getLong("mobile",0)
    }

    private fun setUserDetails() {
        bind.custValue.text = custName
        bind.LoanNoValue.text = loanNo
        bind.branchValue.text = branch
        bind.vehicleValue.text = vehicleNo
        bind.chassisValue.text = chassisNo
        bind.modelValue.text = modelNo
        bind.level1Value.text = level1
        bind.level2Value.text = level2
        bind.level3Value.text = level3
        bind.level4Value.text = level4
        bind.contact1Value.text = contactPerson1
        bind.contact2Value.text = contactPerson2
        bind.contact3Value.text = contactPerson3

    }
    private fun whatsapp(message: String, numbers: List<String>) {
        try {
            val intent = Intent(if (numbers.isNotEmpty()) Intent.ACTION_SENDTO else Intent.ACTION_SEND)
            if (numbers.isNotEmpty()) {
                intent.data = Uri.parse("smsto:${numbers.joinToString(",")}")
                intent.putExtra(Intent.EXTRA_TEXT, message)
                intent.setPackage("com.whatsapp")
            } else {
                intent.type = "text/plain"
                intent.putExtra(Intent.EXTRA_TEXT, message)
                intent.setPackage("com.whatsapp")
            }
            requireContext().startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Please install WhatsApp", Toast.LENGTH_SHORT).show()
        }
    }
    private fun sendMessage(message: String) {
        try {
        // Creating intent with action send
        val intent = Intent(Intent.ACTION_SEND)

        // Setting Intent type
        intent.type = "text/plain"

        // Setting whatsapp package name
        intent.setPackage("com.whatsapp")

        // Give your message here
        intent.putExtra(Intent.EXTRA_TEXT, message)




        // Starting Whatsapp
        startActivity(intent)

        }catch (e: Exception) {
            Toast.makeText(requireContext(), "Please install WhatsApp", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendSMS(message: String, numbers: List<String>) {
        try {
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("smsto:${numbers.joinToString(",")}")
            intent.putExtra("sms_body", message)
            requireContext().startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Failed to send SMS", Toast.LENGTH_SHORT).show()
        }
    }
    private fun contact(number: String) {
        try {
            Log.d("NumberCheck",number)
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:$number")
            requireContext().startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Failed to open dialer", Toast.LENGTH_SHORT).show()
        }
    }
    private fun setOnclickListeners(){
        bind.userWhatsappButton.setOnClickListener{
            sendMessageTo("whatsapp")
        }
        bind.messageButton.setOnClickListener{
            sendMessageTo("message")
        }
        bind.contact1Value.setOnClickListener {
            contact(contactPerson1)
        }
        bind.contact2Value.setOnClickListener {
            contact(contactPerson2)
        }
        bind.contact3Value.setOnClickListener {
            contact(contactPerson3)
        }
        bind.level1Value.setOnClickListener {
            contact(level1)
        }
        bind.level2Value.setOnClickListener {
            contact(level2)
        }
        bind.level3Value.setOnClickListener {
            contact(level3)
        }
        bind.level4Value.setOnClickListener {
            contact(level4)
        }
    }
    private fun sendMessageTo(buttonName: String) {
        val message = getFullMessage()
        val numbers = mutableListOf<String>()
        if (bind.level1CheckBox.isChecked) {
            numbers.add(level1)
        }
        if (bind.level2Checkbox.isChecked) {
            numbers.add(level2)
        }
        if (bind.level3Checkbox.isChecked) {
            numbers.add(level3)
        }
        if (bind.level4Checkbox.isChecked) {
            numbers.add(level4)
        }
        if (bind.checkBoxContact1.isChecked){
            numbers.add(contactPerson1)
        }
        if (bind.checkBoxContact2.isChecked){
            numbers.add(contactPerson2)
        }

        // Print button name, message, and numbers for debugging
        Log.d("Debug", "Button Name: $buttonName")
        Log.d("Debug", "Message: $message")
        Log.d("Debug", "Numbers: $numbers")

        if (buttonName == "whatsapp") {
            sendMessage(message)
        }
        if (buttonName=="message"){
            sendSMS(message,numbers.toList())
        }
    }


    private fun getFullMessage(): String {


        return if(buttonName == "OKRepo"){
            "*Respected Sir*,\n" +
                    "Loan No - *$loanNo*\n" +
                    "Customer Name - *$custName*\n" +
                    "Branch - *$branch*\n" +
                    "Vehicle No - *$vehicleNo*\n" +
                    "Chassis No - *$chassisNo*\n" +
                    "Vehicle Model - *$modelNo*\n" +
                    "Vehicle Location - *$vehicleAddress*\n" +
                    "Load Details - *$carries_goods*\n\n" +
                    "Status: *OK For Repo*\n" +
                    "$name - $mobile\n" +
                    "Agent name- **\n"+
                    "Parking yard- **\n"+
                    "Confirm by- **\n"+
                    "Remark- **\n"+
                    "Agency name: *V K Enterprises*"
        }else if (buttonName=="ConfirmButton"){
            "*Respected Sir*,\n" +
                    "Loan No - *$loanNo*\n" +
                    "Customer Name - *$custName*\n" +
                    "Branch - *$branch*\n" +
                    "Vehicle No - *$vehicleNo*\n" +
                    "Chassis No - *$chassisNo*\n" +
                    "Vehicle Model - *$modelNo*\n" +
                    "Vehicle Location - *$vehicleAddress*\n" +
                    "Load Details - *$carries_goods*\n\n" +
                    "Status: *Please Confirm this Vehicle*\n" +
                    "$name - $mobile\n" +
                    "Agency name: *V K Enterprises*"
        }else{
            "*Respected Sir*,\n" +
                    "Loan No - *$loanNo*\n" +
                    "Customer Name - *$custName*\n" +
                    "Branch - *$branch*\n" +
                    "Vehicle No - *$vehicleNo*\n" +
                    "Chassis No - *$chassisNo*\n" +
                    "Vehicle Model - *$modelNo*\n" +
                    "Vehicle Location - *$vehicleAddress*\n" +
                    "Load Details - *$carries_goods*\n\n" +
                    "Status: *Please Cancel this vehicle*\n" +
                    "$name - $mobile\n" +
                    "Agency name: *V K Enterprises*"
        }
    }
}

