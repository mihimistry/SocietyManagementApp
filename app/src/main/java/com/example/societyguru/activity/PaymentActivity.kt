package com.example.societyguru.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.societyguru.databinding.ActivityPaymentBinding
import com.example.societyguru.utils.MyUtils
import com.payumoney.core.PayUmoneySdkInitializer
import com.payumoney.core.entity.TransactionResponse
import com.payumoney.sdkui.ui.utils.PayUmoneyFlowManager


class PaymentActivity : AppCompatActivity() {

    private lateinit var screen: ActivityPaymentBinding

    private val paymentParamBuilder = PayUmoneySdkInitializer.PaymentParam.Builder()
    private var paymentParam: PayUmoneySdkInitializer.PaymentParam? = null

    private val merchantKey = "your Merchant key"
    private val salt = "your Merchant salt"
    private val merchantId = "your merchant id"
    private var transactionId = "text123456"
    private var amount = ""
    private var charges = ""
    private var totalAmount = ""

    private val productName = "Society Guru"
    private val productInfo = "Society Guru"
    private var generatedHash: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        screen = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(screen.root)

        val b = intent.extras
        b?.let {
            amount = b.getString("amount", "")
            charges = b.getString("charges", "")

            totalAmount = if (charges.isNullOrEmpty())
                amount
            else
                (amount.toInt() + charges.toInt()).toString()


            if (totalAmount.isNotEmpty()) {
                val hashSeq =
                    "$merchantKey|$transactionId|$totalAmount|$productInfo|${MyUtils.getUserFirstName(
                        this
                    )}|${MyUtils.getUserId(
                        this
                    )}|||||||||||$salt"
                generatedHash = MyUtils.hashCal("sha512", hashSeq)
                startPay()
            }
        }
    }

    private fun startPay() {
        paymentParamBuilder.setAmount(totalAmount) // Payment amount
            .setTxnId(transactionId) // Transaction ID
            .setPhone(MyUtils.getUserMobile(this)) // User Phone number
            .setProductName(productName) // Product Name or description
            .setFirstName(MyUtils.getUserFirstName(this)) // User First name
            .setEmail(MyUtils.getUserId(this)) // User Email ID
            .setsUrl("https://www.payumoney.com/mobileapp/payumoney/success.php") // Success URL (surl)
            .setfUrl("https://www.payumoney.com/mobileapp/payumoney/failure.php") //Failure URL (furl)
            .setUdf1("")
            .setUdf2("")
            .setUdf3("")
            .setUdf4("")
            .setUdf5("")
            .setUdf6("")
            .setUdf7("")
            .setUdf8("")
            .setUdf9("")
            .setUdf10("")
            .setIsDebug(true) //under development = True | live = False
            .setKey(merchantKey) // Merchant key
            .setMerchantId(merchantId)
        try {
            paymentParam = paymentParamBuilder.build()
            paymentParam?.setMerchantHash(generatedHash)
            PayUmoneyFlowManager.startPayUMoneyFlow(paymentParam, this, -1, true)
        } catch (e: Exception) {
            Log.e("PAYMENT_ERROR", " error s $e")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PayUmoneyFlowManager.REQUEST_CODE_PAYMENT && resultCode == Activity.RESULT_OK && data != null) {
            val transactionResponse: TransactionResponse? =
                data.getParcelableExtra(PayUmoneyFlowManager.INTENT_EXTRA_TRANSACTION_RESPONSE)
            if (transactionResponse?.getPayuResponse() != null) {
                if (transactionResponse.transactionStatus == TransactionResponse.TransactionStatus.SUCCESSFUL) {
                    setResult(Activity.RESULT_OK)
                }
            }
        }
        finish()
    }


}