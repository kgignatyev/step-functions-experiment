package file.processing.activities

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import org.springframework.stereotype.Service
import software.amazon.awssdk.services.sfn.SfnClient
import software.amazon.awssdk.services.sfn.model.GetActivityTaskRequest
import software.amazon.awssdk.services.sfn.model.SendTaskSuccessRequest
import java.time.Duration
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*


@Service
class ActivityImplementation(val sfnClient: SfnClient, val gson: Gson)  {


    fun readTasks(){
        readTasksForActivity("FileArrivalListener")
        readTasksForActivity("ValidateFile")
        readTasksForActivity("LoadFile")
    }
    fun readTasksForActivity(activityName: String){
        try {
            val request: GetActivityTaskRequest = GetActivityTaskRequest.builder()
                    .activityArn("arn:aws:states:us-west-2:471443061462:activity:${activityName}")
                    .workerName("qds")
                    .overrideConfiguration { it.apiCallAttemptTimeout(Duration.ofSeconds(2)) }
                    .build()

            var taskResponse = sfnClient.getActivityTask(request)
            var input = taskResponse.input()
            while (input != null) {
                val token = taskResponse.taskToken()
                println("""
            $activityName ====================================
            token=            $token
            
            input= $input
            
            
        """.trimIndent())
                taskResponse = sfnClient.getActivityTask(request)
                input = taskResponse.input()
            }
        }catch(e:Exception ){
            println("Ignoring:$e")
        }
    }


    fun sendArrivalSuccess(token: String, info: String){
        val successRequest = SendTaskSuccessRequest.builder()
                .taskToken(token)
                .output(info).build()
        sfnClient.sendTaskSuccess(successRequest)
    }

    fun startWorkflow(partnerId: String, groupId:String, numFilesToExpect: Int){
        val tsFormat = DateTimeFormatter.ofPattern("Y-M-d-H-m-s").withZone(ZoneId.of("UTC"))
        val timestamp = tsFormat.format( Date().toInstant() )
        val inputString = createExecutionInpuFor(partnerId, groupId, numFilesToExpect)
        val execName = "$partnerId-$groupId-$timestamp"
        sfnClient.startExecution{ execBuilder ->
            execBuilder.input(inputString)
            execBuilder.name(execName)
            execBuilder.stateMachineArn("arn:aws:states:us-west-2:471443061462:stateMachine:FileGroupParallelProcessing")
        }
    }

    private fun createExecutionInpuFor(partnerId: String, groupId:String, numFilesToExpect: Int): String {
        val res = JsonObject()
        val fileGroup = JsonObject()
        val jsonArray = JsonArray()
        for(  i in 1.. numFilesToExpect){
            val fileO = JsonObject()
            fileO.addProperty("id", "file-$i-of-$numFilesToExpect-group-${groupId}")
            jsonArray.add( fileO )
        }
        fileGroup.add("files", jsonArray)
        res.add("fileGroup", fileGroup)
        res.addProperty("partnerId", partnerId)
        res.addProperty("groupId", groupId)
        return res.toString()
    }
}
