/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package file.processing.activities

import com.google.gson.Gson
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.services.sfn.SfnClient
import java.io.File

@SpringBootApplication
@Configuration
class App {


    @Bean
    fun stepFunctionsClient():SfnClient{
        return SfnClient.builder().build()
    }

    @Bean
    fun gson(): Gson {
        return Gson()
    }


    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            val cxt = SpringApplication.run( App::class.java)
            val fa = cxt.getBean(ActivityImplementation::class.java)

            if( args.size > 0 ){
                val cmd = args[0]
                if( "start".equals( cmd, true) ){
                    println("starting execution")
                    val partnerId = args[1]
                    val groupId = args[2]
                    val numFilesInTheGroup = args[3].toInt()
                    fa.startWorkflow( partnerId, groupId, numFilesInTheGroup )
                }else if( "read".equals(cmd, true)){
                    fa.readTasks()
                }else if( "send".equals(cmd,true)){
                    val token = args[1]

                    val dataFile = File(if( args.size>2) args[2] else "in.json")
                    val data  = dataFile.readText()

                    fa.sendArrivalSuccess( token, data)
                }else{
                    println("unknown command $cmd")
                }


            }else {
                fa.readTasks()
            }



        }
    }

}