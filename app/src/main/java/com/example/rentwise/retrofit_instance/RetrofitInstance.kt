import android.content.Context
import com.example.rentwise.api_interface.RentWiseApi
import com.example.rentwise.shared_pref_config.TokenManger
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance {
    private const val BASE_URL = "https://rentwiseapi.onrender.com/"

    // Function to create and return an instance of the RentWise API using Retrofit
    fun createAPIInstance(context: Context): RentWiseApi {
        // Create a logging interceptor to log the body of HTTP requests and responses for debugging
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        // Initialises token manager to retrieve stored information within the app
        val tokenManager = TokenManger(context.applicationContext)

        val client = OkHttpClient.Builder()
            //Timeouts to allow the API to 'wake up' upon a request being sent after inactivity
            .connectTimeout(50, TimeUnit.SECONDS)
            .readTimeout(50, TimeUnit.SECONDS)
            .writeTimeout(50, TimeUnit.SECONDS)
            .addInterceptor(logging) //Logging to monitor https requests
            .addInterceptor { chain -> //Interceptor to attach authorisation headers when required
                val requestBuilder = chain.request().newBuilder()
                val token = tokenManager.getToken()
                if (!token.isNullOrEmpty()) { //Assign the token as a bearer authentication header
                    requestBuilder.addHeader("Authorization", "Bearer $token")
                }
                // Proceed with the request including the new header
                chain.proceed(requestBuilder.build())
            }
            .build()

        // Build the Retrofit instance to create the API service
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create()) //Json converter
            .build()
            .create(RentWiseApi::class.java) //Interface
    }
}
