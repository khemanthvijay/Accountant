import { useEffect, useState } from 'react';

type Ledger = { id:string; name:string; ledgerType:string };
type Transaction = { id:string; type:string; date:string; partyId?:string; partyName?:string; reference?:string; notes?:string; amount:number };
const money=(n:number)=>new Intl.NumberFormat('en-IN',{style:'currency',currency:'INR'}).format(n||0);
async function api<T>(url:string):Promise<T>{const r=await fetch(url);if(!r.ok){const b=await r.json().catch(()=>({}));throw new Error(b.message||'Unable to load transactions');}return r.json();}

export function TransactionsScreen({ledgers,onError}:{ledgers:Ledger[];onError:(error:unknown)=>void}) {
 const [from,setFrom]=useState(''),[to,setTo]=useState(''),[partyId,setPartyId]=useState(''),[rows,setRows]=useState<Transaction[]>([]);
 const load=()=>{const q=new URLSearchParams();if(from)q.set('from',from);if(to)q.set('to',to);if(partyId)q.set('partyId',partyId);api<Transaction[]>(`/api/transactions?${q}`).then(setRows).catch(onError);};
 useEffect(()=>{load();},[]);
 return <article><div className="heading"><div><h2>All transactions</h2><p>Sales, purchases, receipts, supplier payments, and expenses in one register.</p></div></div><div className="filters"><label>From<input type="date" value={from} onChange={e=>setFrom(e.target.value)}/></label><label>To<input type="date" value={to} onChange={e=>setTo(e.target.value)}/></label><label>Party / ledger<select value={partyId} onChange={e=>setPartyId(e.target.value)}><option value="">All parties and ledgers</option>{ledgers.map(l=><option value={l.id} key={l.id}>{l.name} ({l.ledgerType.replaceAll('_',' ')})</option>)}</select></label><button onClick={load}>Apply filters</button><button onClick={()=>{setFrom('');setTo('');setPartyId('');api<Transaction[]>('/api/transactions').then(setRows).catch(onError);}}>Clear</button></div><table><thead><tr><th>Date</th><th>Type</th><th>Party / ledger</th><th>Reference</th><th>Notes</th><th>Amount</th></tr></thead><tbody>{rows.map(row=><tr key={`${row.type}-${row.id}`}><td>{row.date}</td><td>{row.type.replaceAll('_',' ')}</td><td>{row.partyName||'-'}</td><td>{row.reference||'-'}</td><td>{row.notes||'-'}</td><td>{money(row.amount)}</td></tr>)}{!rows.length&&<tr><td colSpan={6}>No transactions match these filters.</td></tr>}</tbody></table></article>;
}
