package dev.projectos.bloomowner
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONArray
import org.json.JSONObject

private val bg=Color(0xFF080D16); private val panel=Color(0xFF142033); private val raised=Color(0xFF203047)
private val white=Color(0xFFF4F6FA); private val muted=Color(0xFFAAB7C8); private val accent=Color(0xFFB8D8F2); private val warn=Color(0xFFFFB5A8)
class OwnerActivity:ComponentActivity(){override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);setContent{MaterialTheme(colorScheme=darkColorScheme(primary=accent,background=bg,surface=panel)){Console()}}}}
private enum class P(val label:String,val group:String){HOME("Overview","COMMAND"),GLOBAL("Global operations","COMMAND"),FLEET("Test device fleet","TEST OS"),BROADCAST("Broadcast studio","TEST OS"),ALERT("Emergency alerts","TEST OS"),ENFORCE("Trust & enforcement","SAFETY"),POLICY("App & policy control","PLATFORM"),RELEASE("Release manager","PLATFORM"),DIAG("Diagnostics","PLATFORM"),AUDIT("Command history","SECURITY"),SETTINGS("Owner settings","SECURITY")}
private data class Cmd(val time:String,val title:String,val scope:String,val status:String)
private fun read(p:android.content.SharedPreferences):List<Cmd>{val a=JSONArray(p.getString("queue","[]")?:"[]");return (0 until a.length()).map{val o=a.getJSONObject(it);Cmd(o.optString("time"),o.optString("title"),o.optString("scope"),o.optString("status"))}}
private fun add(p:android.content.SharedPreferences,title:String,scope:String){val a=JSONArray();(listOf(Cmd(java.text.SimpleDateFormat("HH:mm:ss",java.util.Locale.UK).format(java.util.Date()),title,scope,"WAITING FOR SECURE BACKEND"))+read(p).take(78)).forEach{a.put(JSONObject().put("time",it.time).put("title",it.title).put("scope",it.scope).put("status",it.status))};p.edit().putString("queue",a.toString()).apply()}
@Composable private fun Console(){
 val ctx=androidx.compose.ui.platform.LocalContext.current;val prefs=remember{ctx.getSharedPreferences("bloom_owner_console",0)}
 var page by remember{mutableStateOf(P.HOME)};var revision by remember{mutableIntStateOf(0)};var name by remember{mutableStateOf(prefs.getString("owner","Owner")?:"Owner")}
 BackHandler(page!=P.HOME){page=P.HOME}
 Row(Modifier.fillMaxSize().background(bg).padding(15.dp),horizontalArrangement=Arrangement.spacedBy(14.dp)){
  Column(Modifier.width(220.dp).fillMaxHeight().background(panel,RoundedCornerShape(20.dp)).padding(13.dp)){
   Text("✿ BLOOM",color=accent,fontSize=21.sp,fontWeight=FontWeight.Black,letterSpacing=2.sp);Text("COMMAND CENTRE",color=muted,fontSize=9.sp,letterSpacing=2.sp);Spacer(Modifier.height(18.dp))
   P.values().groupBy{it.group}.forEach{(g,items)->Text(g,color=accent,fontSize=9.sp,fontWeight=FontWeight.Bold,modifier=Modifier.padding(start=8.dp,top=10.dp,bottom=5.dp));items.forEach{s->Text(s.label,color=if(page==s)accent else white,fontSize=12.sp,fontWeight=if(page==s)FontWeight.Bold else FontWeight.Normal,modifier=Modifier.fillMaxWidth().background(if(page==s)accent.copy(alpha=.13f)else Color.Transparent,RoundedCornerShape(8.dp)).clickable{page=s}.padding(10.dp))}}
   Spacer(Modifier.weight(1f));Text("PRIVATE OWNER CONSOLE",color=Color(0xFF9ED7B2),fontSize=9.sp);Text("TEST ENVIRONMENT ONLY",color=muted,fontSize=9.sp)
  }
  Column(Modifier.weight(1f).fillMaxHeight()){
   Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){Column{Text(page.label,color=white,fontSize=26.sp,fontWeight=FontWeight.Bold);Text("Bloom platform / "+page.group.lowercase()+" workspace",color=muted,fontSize=11.sp)};Text(name+" · OWNER",color=accent,fontSize=11.sp)}
   Spacer(Modifier.height(13.dp))
   when(page){
    P.HOME->Home(prefs,{page=it},revision)
    P.GLOBAL->Global(prefs){revision++}
    P.FLEET->Fleet(prefs){revision++}
    P.BROADCAST->Broadcast(prefs){revision++}
    P.ALERT->Alerts(prefs){revision++}
    P.ENFORCE->Enforcement(prefs){revision++}
    P.POLICY->Policy(prefs){revision++}
    P.RELEASE->ReleasePage()
    P.DIAG->Diagnostics()
    P.AUDIT->Audit(prefs,revision){prefs.edit().remove("queue").apply();revision++}
    P.SETTINGS->Settings(name){name=it;prefs.edit().putString("owner",it).apply()}
   }
  }
 }
}
@Composable private fun Page(content:@Composable ColumnScope.()->Unit){Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(11.dp),content=content)}
@Composable private fun Panel(title:String,desc:String?=null,content:@Composable ColumnScope.()->Unit){Column(Modifier.fillMaxWidth().background(panel,RoundedCornerShape(15.dp)).padding(15.dp),verticalArrangement=Arrangement.spacedBy(9.dp)){Text(title,color=white,fontSize=17.sp,fontWeight=FontWeight.Bold);if(desc!=null)Text(desc,color=muted,fontSize=11.sp);content()}}
@Composable private fun Btn(label:String,action:()->Unit){Button(onClick=action,colors=ButtonDefaults.buttonColors(containerColor=accent,contentColor=bg),contentPadding=PaddingValues(horizontal=12.dp,vertical=8.dp)){Text(label,fontSize=11.sp,fontWeight=FontWeight.Bold)}}
@Composable private fun Input(value:String,change:(String)->Unit,hint:String,multi:Boolean=false){BasicTextField(value,change,modifier=Modifier.fillMaxWidth().then(if(multi)Modifier.heightIn(min=80.dp)else Modifier).background(raised,RoundedCornerShape(8.dp)).padding(12.dp),textStyle=androidx.compose.ui.text.TextStyle(color=white,fontSize=12.sp),decorationBox={inner->if(value.isEmpty())Text(hint,color=muted,fontSize=12.sp);inner()})}
@Composable private fun Note(s:String,c:Color=muted){Text(s,color=c,fontSize=10.sp)}
@Composable private fun Metric(a:String,b:String,c:String){Column(Modifier.weight(1f).background(panel,RoundedCornerShape(12.dp)).padding(12.dp)){Text(a,color=muted,fontSize=9.sp);Text(b,color=accent,fontSize=21.sp,fontWeight=FontWeight.Bold);Text(c,color=muted,fontSize=9.sp)}}
@Composable private fun Home(p:android.content.SharedPreferences,open:(P)->Unit,rev:Int){val q=remember(rev){read(p)};Page{
 Box(Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(Color(0xFF314B69),Color(0xFF19283B),bg)),RoundedCornerShape(18.dp)).padding(20.dp)){Column{Text("OWNER OPERATIONS",color=accent,fontSize=10.sp,letterSpacing=2.sp);Text("Your test platform, under control.",color=white,fontSize=25.sp,fontWeight=FontWeight.Bold);Text("A private control surface for Bloom Test OS. Main Bloom OS remains separate.",color=muted,fontSize=12.sp)}}
 Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){Metric("TARGET","TEST OS","Separate app");Metric("COMMANDS",q.size.toString(),"Local queue");Metric("REMOTE DEVICES","0","No service connected");Metric("MAIN OS","ISOLATED","Not targeted")}
 Panel("Quick actions","Queue test-environment operations."){Row(horizontalArrangement=Arrangement.spacedBy(7.dp)){Btn("Global controls"){open(P.GLOBAL)};Btn("Priority alert"){open(P.ALERT)};Btn("Enforcement"){open(P.ENFORCE)}};Row(horizontalArrangement=Arrangement.spacedBy(7.dp)){Btn("Broadcast"){open(P.BROADCAST)};Btn("Fleet"){open(P.FLEET)};Btn("Audit"){open(P.AUDIT)}}}
 Panel("Latest commands"){q.take(4).forEach{Note(it.title+" · "+it.status,white)};if(q.isEmpty())Note("No commands queued.")}
 Note("Commands are saved locally and NOT delivered to other TVs until an authenticated service and enrolled agents exist.",warn)
}}
@Composable private fun Global(p:android.content.SharedPreferences,changed:()->Unit){val ops=listOf("Enable Test OS maintenance mode","Resume Test OS services","Request Test OS restart","Refresh test configuration","Pause test enrolments","Resume test enrolments","Freeze test release rollout","Resume test release rollout","Set test environment read-only","Clear test announcement banner","Request fleet diagnostics","Restore normal test operation");Page{Panel("Global test operations","These target Bloom Test OS only. Each creates an auditable local command."){ops.forEach{s->Row(Modifier.fillMaxWidth().background(raised,RoundedCornerShape(8.dp)).padding(9.dp),verticalAlignment=Alignment.CenterVertically){Text(s,color=white,fontSize=11.sp,modifier=Modifier.weight(1f));Btn("Queue"){add(p,s,"All enrolled Test OS devices");changed()}}}};Note("No physical power-off controls. Restart and shutdown are service-level test requests, not hardware power commands.",warn)}}
@Composable private fun Fleet(p:android.content.SharedPreferences,changed:()->Unit){Page{Panel("Test device fleet","Remote fleet requires test-device enrolment and a secure service."){Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){Metric("ENROLLED","0","Devices");Metric("ONLINE","0","Heartbeat");Metric("RESTRICTED","0","Active cases")};listOf("Create enrolment invite","Rotate enrolment token","Request device inventory","Refresh fleet heartbeat","Export device register").forEach{s->Row(verticalAlignment=Alignment.CenterVertically){Text(s,color=white,fontSize=11.sp,modifier=Modifier.weight(1f));TextButton(onClick={add(p,s,"Test fleet");changed()}){Text("Queue",color=accent)}}};Note("No remote devices are connected. Main Bloom OS is excluded.")}}}
@Composable private fun Broadcast(p:android.content.SharedPreferences,changed:()->Unit){var title by remember{mutableStateOf("")};var msg by remember{mutableStateOf("")};var audience by remember{mutableStateOf("All enrolled Test OS devices")};Page{Panel("Broadcast studio","Prepare a test-environment notice."){Input(title,{title=it},"Announcement title");Input(msg,{msg=it},"Message",true);listOf("All enrolled Test OS devices","Selected test devices","Test administrators only").forEach{Row(verticalAlignment=Alignment.CenterVertically){RadioButton(audience==it,{audience=it});Text(it,color=white,fontSize=11.sp)}};Btn("Queue announcement"){if(title.isNotBlank()&&msg.isNotBlank()){add(p,"Announcement: "+title+" | "+msg,audience);changed()}};Note("Local queue only. Not sent to other devices.",warn)}}}
@Composable private fun Alerts(p:android.content.SharedPreferences,changed:()->Unit){var title by remember{mutableStateOf("")};var msg by remember{mutableStateOf("")};var sound by remember{mutableStateOf(true)};var status by remember{mutableStateOf("")};Page{Panel("Bloom priority alert","For genuine Bloom service incidents only. Not an official public emergency alert."){Input(title,{title=it},"Alert heading");Input(msg,{msg=it},"Alert message",true);Row(verticalAlignment=Alignment.CenterVertically){Text("Original Bloom warning tone",color=white,modifier=Modifier.weight(1f));Switch(sound,{sound=it})};Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){Btn("Preview tone"){try{val t=ToneGenerator(AudioManager.STREAM_NOTIFICATION,55);t.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD,900);android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({t.release()},1100);status="Local tone preview played."}catch(e:Exception){status="Preview unavailable."}};Btn("Queue alert"){if(title.isNotBlank()&&msg.isNotBlank()){add(p,"Priority alert: "+title+" | sound="+sound,"Bloom Test OS");changed();status="Queued locally, not delivered."}}};Note(status,accent)};Note("Uses a short original warning tone, not a copy of the UK Emergency Alerts signal. Remote delivery needs enrolled devices and a secure service.",warn)}}
@Composable private fun Enforcement(p:android.content.SharedPreferences,changed:()->Unit){var device by remember{mutableStateOf("")};var reason by remember{mutableStateOf("")};var evidence by remember{mutableStateOf("")};var duration by remember{mutableStateOf("24 hours")};var result by remember{mutableStateOf("")};Page{Panel("Trust & enforcement","Evidence-led review, temporary restrictions, appeals and owner recovery."){Input(device,{device=it},"Enrolled Test OS device ID");Input(reason,{reason=it},"Policy concern / incident summary",true);Input(evidence,{evidence=it},"Evidence reference and reviewer notes",true);Text("Proposed restriction period",color=muted,fontSize=11.sp);listOf("1 hour","24 hours","7 days","Until manual review").forEach{Row(verticalAlignment=Alignment.CenterVertically){RadioButton(duration==it,{duration=it});Text(it,color=white,fontSize=11.sp)}};Btn("Create review case"){if(device.isNotBlank()&&reason.isNotBlank()){add(p,"Review case: "+reason+" | evidence: "+evidence,"Test device "+device);changed();result="Review case recorded locally."}};Btn("Queue temporary restriction request"){if(device.isNotBlank()&&reason.isNotBlank()){add(p,"Restriction request ("+duration+"): "+reason,"Test device "+device);changed();result="Request recorded. No device has been locked."}};Note(result,warn)};Panel("Enforcement safeguards"){listOf("Only enrolled Bloom Test OS devices can be targeted.","Require documented reasons and evidence.","Provide an appeal and owner recovery route.","Use expiry and review dates.","Do not restrict on an unverified accusation.").forEach{Note("• "+it)}}}}
@Composable private fun Policy(p:android.content.SharedPreferences,changed:()->Unit){val policies=listOf("Require verified app sources","Block unapproved test builds","Require privacy notice acceptance","Restrict test-only developer menus","Set minimum supported version","Review requested permissions","Enable safe-mode recovery","Require release approval");Page{Panel("App and policy manager","Policy proposals are not enforced until a policy service is connected."){policies.forEach{s->var on by remember{mutableStateOf(false)};Row(verticalAlignment=Alignment.CenterVertically){Text(s,color=white,fontSize=11.sp,modifier=Modifier.weight(1f));Switch(on,{on=it;add(p,"Policy proposal: "+s+" = "+it,"Test OS policy");changed()})};Note("PROPOSAL · NOT ENFORCED")}}}}
@Composable private fun ReleasePage(){Page{Panel("Release management","Controlled test release rings."){listOf("Internal preview","Canary test devices","Wider test cohort","Stable test channel","Rollback planning","Release notes","Compatibility gate").forEach{Row(Modifier.fillMaxWidth().background(raised,RoundedCornerShape(8.dp)).padding(12.dp)){Text(it,color=white,modifier=Modifier.weight(1f));Note("NOT CONNECTED",warn)}};Note("GitHub Actions builds APKs. This screen does not silently install or roll back software.")}}}
@Composable private fun Diagnostics(){Page{Panel("Diagnostics","Current integration status."){listOf("Owner console: local","Secure owner service: not configured","Test device agents: none enrolled","Alert delivery: unavailable","Remote enforcement: unavailable","Main OS: separate application ID").forEach{Note("• "+it)}}}}
@Composable private fun Audit(p:android.content.SharedPreferences,rev:Int,clear:()->Unit){val q=remember(rev){read(p)};Page{Panel("Command history","Newest records first. Queueing is not delivery."){if(q.isEmpty())Note("No command records.");q.forEach{Column(Modifier.fillMaxWidth().background(raised,RoundedCornerShape(8.dp)).padding(10.dp)){Text(it.title,color=white,fontSize=12.sp,fontWeight=FontWeight.SemiBold);Note(it.time+" · "+it.scope);Note(it.status,warn)}};Btn("Clear local history",clear)}}}
@Composable private fun Settings(name:String,change:(String)->Unit){Page{Panel("Owner preferences","Stored on this installation."){Input(name,change,"Owner display name");Note("Secure your Android device with a screen lock. Server-backed owner authentication is not yet configured.")}}}
