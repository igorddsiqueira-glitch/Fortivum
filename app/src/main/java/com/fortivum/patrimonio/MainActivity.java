package com.fortivum.patrimonio;

import android.app.*;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.content.*;
import android.view.*;
import android.widget.*;
import org.json.*;
import java.text.*;
import java.util.*;
import java.net.*;
import java.io.*;
import android.os.Handler;

public class MainActivity extends Activity {
  final int BG=Color.rgb(13,17,25), PANEL=Color.rgb(25,31,43), GOLD=Color.rgb(215,176,92), WHITE=Color.rgb(246,246,242), MUTED=Color.rgb(163,174,191);
  static final String API_BASE="https://loja-com.com/fortivum-api"; // SUBSTITUIR PELO SEU HTTPS
  final Handler handler=new Handler();
  String deviceId;
  long validUntil=0;
  LinearLayout root, content; JSONArray items=new JSONArray(); boolean hidden=false;
  String[] types={"Imóvel","Veículo","Investimento","Outros bens","Dívida"};
  @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().setStatusBarColor(BG);getWindow().setNavigationBarColor(BG);load();deviceId=getPreferences(0).getString("deviceId", "");if(deviceId.isEmpty()){deviceId=UUID.randomUUID().toString();getPreferences(0).edit().putString("deviceId",deviceId).apply();}checkLicense();}
  void load(){try{items=new JSONArray(getPreferences(0).getString("items","[]"));hidden=getPreferences(0).getBoolean("hidden",false);}catch(Exception e){items=new JSONArray();}}
  void save(){getPreferences(0).edit().putString("items",items.toString()).putBoolean("hidden",hidden).apply();}
  GradientDrawable shape(int color,int radius){GradientDrawable g=new GradientDrawable();g.setColor(color);g.setCornerRadius(radius);return g;}
  TextView text(String s,int size,int color,boolean bold){TextView v=new TextView(this);v.setText(s);v.setTextSize(size);v.setTextColor(color);if(bold)v.setTypeface(null,Typeface.BOLD);return v;}
  LinearLayout vertical(){LinearLayout l=new LinearLayout(this);l.setOrientation(1);return l;}
  void pad(View v,int p){v.setPadding(p,p,p,p);}
  String money(double v){return NumberFormat.getCurrencyInstance(new Locale("pt","BR")).format(v);}
  double sum(String t){double total=0;for(int i=0;i<items.length();i++){JSONObject o=items.optJSONObject(i);if(o!=null && t.equals(o.optString("type")))total+=o.optDouble("value");}return total;}
  double assets(){double a=0;for(String t:types)if(!t.equals("Dívida"))a+=sum(t);return a;}
  String shown(double v){return hidden?"••••••":money(v);}
  void checkLicense(){
    if(API_BASE.contains("SEU-DOMINIO")){showPaywall("Configure o endereço do servidor para ativar o Pix.");return;}
    api("/status.php", "device_id="+deviceId, (json)->{
      validUntil=json.optLong("valid_until",0);
      if(json.optBoolean("active",false)){render();}else showPaywall("Ative seu acesso anual por apenas R$ 1,00.");
    });
  }
  interface Callback { void done(JSONObject obj); }
  void api(String path,String body,Callback callback){
    new Thread(()->{
      JSONObject result=new JSONObject();
      try{
        URL url=new URL(API_BASE+path);
        HttpURLConnection conn=(HttpURLConnection)url.openConnection();
        conn.setRequestMethod("POST");conn.setConnectTimeout(12000);conn.setReadTimeout(12000);
        conn.setDoOutput(true);conn.setRequestProperty("Content-Type","application/x-www-form-urlencoded; charset=UTF-8");
        try(OutputStream out=conn.getOutputStream()){out.write(body.getBytes("UTF-8"));}
        try(InputStream in=conn.getInputStream();ByteArrayOutputStream buf=new ByteArrayOutputStream()){
          byte[] chunk=new byte[2048];int n;while((n=in.read(chunk))!=-1)buf.write(chunk,0,n);
          result=new JSONObject(buf.toString("UTF-8"));
        }
      }catch(Exception ex){try{result.put("error","Falha na conexão com o servidor. Verifique a internet e a configuração.");}catch(Exception ignored){}}
      final JSONObject output=result;runOnUiThread(()->callback.done(output));
    }).start();
  }
  void showPaywall(String message){
    ScrollView sc=new ScrollView(this);sc.setBackgroundColor(BG);root=vertical();pad(root,25);sc.addView(root);setContentView(sc);
    space(25);root.addView(text("◆ FORTIVUM",28,GOLD,true));space(12);
    root.addView(text("Seu patrimônio sob controle",24,WHITE,true));space(18);
    for(String line:new String[]{"✓ Imóveis, veículos e investimentos","✓ Dívidas e patrimônio líquido","✓ Gráficos por categoria","✓ Backup manual dos seus registros","✓ Privacidade: dados no aparelho"}){
      root.addView(text(line,17,WHITE,false));space(14);
    }
    space(12);root.addView(text("R$ 1,00 / 365 dias",28,GOLD,true));space(8);
    root.addView(text("Renovação manual. Sem cobrança automática. Requer internet para verificar a licença.",13,MUTED,false));space(14);
    root.addView(text(message,14,MUTED,false));space(18);
    Button pay=button("Pagar R$ 1,00 via Pix");root.addView(pay);
    pay.setOnClickListener(v->{pay.setEnabled(false);api("/checkout.php","device_id="+deviceId,(obj)->{
      pay.setEnabled(true);String url=obj.optString("url","");
      if(url.startsWith("https://checkout.infinitepay.com.br/")){
        startActivity(new Intent(Intent.ACTION_VIEW,android.net.Uri.parse(url)));
      }else Toast.makeText(this,obj.optString("error","Não foi possível iniciar o Pix"),Toast.LENGTH_LONG).show();
    });});space(10);
    Button verify=button("Já paguei — verificar acesso");root.addView(verify);verify.setOnClickListener(v->checkLicense());
  }
  void render(){
    ScrollView sc=new ScrollView(this);sc.setFillViewport(true);sc.setBackgroundColor(BG);root=vertical();pad(root,22);sc.addView(root);setContentView(sc);
    TextView logo=text("◆  FORTIVUM",24,GOLD,true);root.addView(logo);
    TextView sub=text("PATRIMÔNIO SOB CONTROLE",11,MUTED,false);sub.setLetterSpacing(.14f);root.addView(sub);
    space(25);
    LinearLayout summary=vertical();pad(summary,22);summary.setBackground(shape(PANEL,24));root.addView(summary);
    summary.addView(text("PATRIMÔNIO LÍQUIDO",12,MUTED,true));spaceIn(summary,9);
    summary.addView(text(shown(assets()-sum("Dívida")),31,GOLD,true));spaceIn(summary,14);
    summary.addView(text("Bens e investimentos: "+shown(assets()),14,WHITE,false));
    summary.addView(text("Dívidas: "+shown(sum("Dívida")),14,MUTED,false));
    space(15);
    Button toggle=button(hidden?"Mostrar valores":"Ocultar valores");root.addView(toggle);toggle.setOnClickListener(v->{hidden=!hidden;save();render();});
    space(18);root.addView(text("Distribuição patrimonial",20,WHITE,true));space(12);
    for(String t:types){double amount=sum(t);if(amount>0){LinearLayout r=vertical();pad(r,12);r.setBackground(shape(PANEL,18));root.addView(r);r.addView(text(t+"  •  "+shown(amount),15,WHITE,true));if(!t.equals("Dívida")&&assets()>0){ProgressBar pb=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);pb.setMax(100);pb.setProgress((int)Math.round(100*amount/assets()));r.addView(pb);}space(7);}}
    space(18);Button add=button("+ Adicionar bem ou dívida");root.addView(add);add.setOnClickListener(v->addDialog());
    space(23);root.addView(text("Meus registros",20,WHITE,true));space(10);
    if(items.length()==0){root.addView(text("Nenhum registro ainda. Cadastre seu primeiro bem para começar.",14,MUTED,false));}
    for(int i=0;i<items.length();i++){JSONObject o=items.optJSONObject(i);if(o==null)continue;final int idx=i;LinearLayout r=vertical();pad(r,16);r.setBackground(shape(PANEL,16));root.addView(r);r.addView(text(o.optString("name"),17,WHITE,true));r.addView(text(o.optString("type")+"  •  "+shown(o.optDouble("value")),14,GOLD,false));r.setOnClickListener(v->new AlertDialog.Builder(this).setTitle(o.optString("name")).setMessage("Excluir este registro?").setNegativeButton("Cancelar",null).setPositiveButton("Excluir",(d,w)->{JSONArray next=new JSONArray();for(int j=0;j<items.length();j++)if(j!=idx)next.put(items.opt(j));items=next;save();render();}).show());space(8);}
    space(16);root.addView(text("Licença até: "+new java.text.SimpleDateFormat("dd/MM/yyyy",new Locale("pt","BR")).format(new Date(validUntil*1000)),12,GOLD,false));
    space(24);root.addView(text("Dados salvos apenas neste aparelho. Faça backup antes de desinstalar.",12,MUTED,false));
    space(8);Button export=button("Compartilhar backup JSON");root.addView(export);export.setOnClickListener(v->{Intent intent=new Intent(Intent.ACTION_SEND);intent.setType("text/plain");intent.putExtra(Intent.EXTRA_TEXT,items.toString());startActivity(Intent.createChooser(intent,"Salvar backup"));});
    space(8);Button restore=button("Restaurar backup JSON");root.addView(restore);restore.setOnClickListener(v->restoreDialog());
  }
  Button button(String label){Button b=new Button(this);b.setText(label);b.setTextColor(BG);b.setAllCaps(false);b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(GOLD));return b;}
  void space(int h){View v=new View(this);root.addView(v,new LinearLayout.LayoutParams(1,h));}
  void spaceIn(LinearLayout l,int h){View v=new View(this);l.addView(v,new LinearLayout.LayoutParams(1,h));}
  void addDialog(){LinearLayout form=vertical();pad(form,20);EditText name=new EditText(this);name.setHint("Descrição (ex.: Apartamento Centro)");form.addView(name);Spinner spinner=new Spinner(this);spinner.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,types));form.addView(spinner);EditText val=new EditText(this);val.setHint("Valor em R$ (ex.: 250000,50)");val.setInputType(8194);form.addView(val);
    new AlertDialog.Builder(this).setTitle("Novo registro").setView(form).setNegativeButton("Cancelar",null).setPositiveButton("Salvar",(d,w)->{try{String n=name.getText().toString().trim();double amount=Double.parseDouble(val.getText().toString().trim().replace(".","").replace(",","."));if(n.isEmpty()||amount<=0||Double.isInfinite(amount)||Double.isNaN(amount))throw new Exception();JSONObject o=new JSONObject();o.put("name",n);o.put("type",types[spinner.getSelectedItemPosition()]);o.put("value",amount);items.put(o);save();render();}catch(Exception e){Toast.makeText(this,"Informe descrição e valor positivo válido",Toast.LENGTH_LONG).show();}}).show();
  }
  void restoreDialog(){EditText input=new EditText(this);input.setHint("Cole aqui o conteúdo JSON do backup");new AlertDialog.Builder(this).setTitle("Restaurar backup").setMessage("Substituirá os registros atuais.").setView(input).setNegativeButton("Cancelar",null).setPositiveButton("Restaurar",(d,w)->{try{JSONArray a=new JSONArray(input.getText().toString());for(int i=0;i<a.length();i++){JSONObject o=a.getJSONObject(i);if(o.optString("name").trim().isEmpty()||!Arrays.asList(types).contains(o.optString("type"))||o.optDouble("value")<=0)throw new Exception();}items=a;save();render();}catch(Exception e){Toast.makeText(this,"Backup inválido",Toast.LENGTH_LONG).show();}}).show();}
}
