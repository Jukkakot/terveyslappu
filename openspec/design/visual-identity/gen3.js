const {chromium}=require('playwright');
const fs=require('fs');
const OUT=__dirname+'/';
const SHOTS='/tmp/claude-0/-home-user-terveyslappu/2ea1c568-cf61-5557-8d73-66f549c8004a/scratchpad/shots/';
const H=SHOTS+'01-home-dark.png',P=SHOTS+'10-history-paino-dark.png',S=SHOTS+'11-history-energia-dark.png';
const RX=784, RY=96; // right frame origin in compare px (2x of css px, body clip starts at css y=28)

// right-frame position from an element rect: fx,fy fractions, dx,dy px offsets (compare px)
const E=(sel,fx,fy,dx=0,dy=0,idx=0)=>({sel,fx,fy,dx,dy,idx});
const A_=(x,y)=>({abs:[x,y]});

const LEFTX=784; // badge column on the left edge of the right frame
const rowL=(sel,idx=0)=>({sel,idx,leftcol:true});

const NEXT_TXT={
 C:'C: kaikki minttua ja hehkua.',
 AC:'AC: A:n värikoodatut ikonit ja kortit + C:n musta pohja.',
 A:'A: pehmeämpi harmaa pohja, minttu korostuksena.'
};

const paino=(dir)=>({
 file:`${dir}-historia-paino-dark`,name:`vertailu-historia-paino-${dir}`,old:P,l2:dir,
 pts:[
  {n:1,t:'Välilehdet ylimpänä, kaikki viisi mahtuvat',new:rowL('.tabs'),old:A_(392,337)},
  {n:2,t:'Jaksot yhdellä rivillä, ei leikkaudu',new:rowL('.seg'),old:A_(34,272)},
  {n:3,t:'Trendiviiva: harmaa pehmeä viiva näyttää suunnan vuoden yli',new:A_(1112,898)},
  {n:4,t:'Päivän tiedoissa myös trendi',new:A_(1222,512)},
  {n:5,t:'Muutos isona lukuna, muut luvut yhdellä rivillä',new:rowL('.hero'),extra:[rowL('.kv')],old:A_(40,1095)},
  {n:6,t:'Punaiset roskakorit vaihtuneet ⋮-valikkoon',new:E('.li .more',.5,.5,58,0),old:A_(744,1296)}
 ],dirNote:NEXT_TXT[dir]
});
const syoty=(dir)=>({
 file:`${dir}-historia-syoty-dark`,name:`vertailu-historia-syoty-${dir}`,old:S,l2:dir,
 pts:[
  {n:1,t:'Välilehdet ylimpänä, kaikki viisi mahtuvat',new:rowL('.tabs'),old:A_(392,337)},
  {n:2,t:'Jaksot yhdellä rivillä, ei leikkaudu',new:rowL('.seg'),old:A_(34,272)},
  {n:3,t:'Trendiviiva: pehmeä viiva näyttää suunnan vuoden yli',new:A_(992,566)},
  {n:4,t:'Valitun viikon tiedot omassa kortissa',new:A_(1176,496)},
  {n:5,t:'Keskiarvo isona lukuna, muut luvut yhdellä rivillä',new:rowL('.hero'),extra:[rowL('.kv')],old:A_(40,1080)},
  {n:6,t:'Merkintä viikoittain: pylväs on viikon keskiarvo',new:A_(1400,918)}
 ],dirNote:NEXT_TXT[dir]
});

const CFG=[
 {file:'AC-etusivu-dark',name:'vertailu-etusivu-AC',old:H,l2:'AC',
  pts:[
   {n:1,t:'Kortin otsikot eivät leikkaudu (Syöty, Askeleet kokonaan)',new:A_(1143,957),extra:[A_(784,957)],old:A_(392,828)},
   {n:2,t:'Väri per suure: paino minttu, ruoka meripihka, askeleet sininen',new:A_(784,284),extra:[A_(784,1232),A_(1143,1232)],old:A_(40,276)},
   {n:3,t:'Isompi, lihavampi numero',new:A_(1190,388),old:A_(330,345)},
   {n:4,t:'Hehkuva painoviiva pehmeällä täytöllä',new:A_(960,474),old:A_(330,468)},
   {n:5,t:'Tämä päivä korostettu kirkkaalla värillä',new:A_(1412,570),extra:[A_(1088,1192),A_(1425,1210)],old:A_(648,494)}
  ],dirNote:NEXT_TXT.AC},
 paino('A'),paino('C'),paino('AC'),syoty('C'),syoty('AC')
];

const badge=(n,x,y)=>`<div class="bd" style="left:${x-26}px;top:${y-26}px">${n}</div>`;

(async()=>{
 const b=await chromium.launch();
 const ctx=await b.newContext({viewport:{width:360,height:808},deviceScaleFactor:2});
 const fp=await ctx.newPage();
 for(const c of CFG){
  await fp.goto('file://'+OUT+c.file+'.html');await fp.waitForTimeout(250);
  const place=async(spec)=>{
   if(spec.abs) return spec.abs;
   const r=await fp.evaluate(([s,i])=>{const e=document.querySelectorAll(s)[i];const r=e.getBoundingClientRect();return {x:r.x,y:r.y,w:r.width,h:r.height}},[spec.sel,spec.idx||0]);
   const cy=RY+(r.y-28)*2+r.h;
   if(spec.leftcol) return [LEFTX,RY+(r.y-28)*2+r.h];
   return [RX+(r.x+spec.fx*r.w)*2+spec.dx, RY+(r.y-28+spec.fy*r.h)*2+spec.dy];
  };
  let bad='';
  for(const p of c.pts){
   const list=[p.new,...(p.extra||[])];
   for(const s of list){const [x,y]=await place(s);bad+=badge(p.n,x,y);}
   if(p.old){const [x,y]=p.old.abs;bad+=badge(p.n,x,y);}
  }
  const items=c.pts.map(p=>`<div class="it"><div class="bd s">${p.n}</div><div class="tx">${p.t}</div></div>`).join('');
  const html=`<!doctype html><html><head><meta charset="utf-8"><style>
  *{margin:0;box-sizing:border-box}body{background:#2a2c2e;width:1536px;font-family:Roboto,sans-serif;position:relative}
  .top{display:flex;gap:32px;padding:32px 32px 0}
  .f{width:720px}.c{color:#fff;font:600 34px/56px Roboto,sans-serif;height:64px}.f img{width:720px;height:1560px;display:block}
  .bd{position:absolute;width:52px;height:52px;border-radius:50%;background:#FFD54F;color:#000;font:700 30px/48px Roboto,sans-serif;text-align:center;border:2px solid #111;box-shadow:0 0 0 1px rgba(0,0,0,.5),0 3px 8px rgba(0,0,0,.65)}
  .bd.s{position:static;flex:none;margin-right:24px}
  .pn{margin:32px;background:#121416;border-radius:28px;padding:36px 40px 40px}
  .it{display:flex;align-items:center;min-height:68px;margin-bottom:10px}
  .tx{color:#F2F5F7;font:500 38px/46px Roboto,sans-serif}
  .dn{margin-top:22px;padding-top:26px;border-top:2px solid #2d3238;color:#FFD54F;font:600 38px/46px Roboto,sans-serif}
  </style></head><body>
  <div class="top"><div class="f"><div class="c">Nykyinen</div><img src="file://${c.old}"></div><div class="f"><div class="c">${c.l2}</div><img src="file://${OUT+c.file}-body.png"></div></div>
  ${bad}
  <div class="pn">${items}<div class="dn">${c.dirNote}</div></div>
  </body></html>`;
  fs.writeFileSync(OUT+c.name+'.html',html);
  const pg=await b.newPage({viewport:{width:1536,height:1688},deviceScaleFactor:1});
  await pg.goto('file://'+OUT+c.name+'.html');await pg.waitForTimeout(300);
  await pg.screenshot({path:OUT+c.name+'.png',fullPage:true});await pg.close();
 }
 await b.close();
})().catch(e=>{console.error(e);process.exit(1)});
