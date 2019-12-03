import tkinter as tk
import requests

IP = 'baidu.com'


def func_buy():
    name = Region1.get()
    id = Region2.get()
    num = int(Region3.get())
    my_data = {
        'product_id': int(id),
        'product_name': name,
        'product_inventory': num
    }
    response = requests.post(IP, data=my_data)
    txt_result.delete('0.0', 'end')
    print(response.text)
    txt_result.insert('end', response.text)


def func_query():
    id = Region4.get()
    my_data = {
        'sql': 'select inventory from table where product_id=' + id
    }
    response = requests.post(IP, data=my_data)
    txt_result.delete('0.0', 'end')
    print(response.text)
    txt_result.insert('end', response.text)


def func_add():
    id = Region5.get()
    num=Region6.get()
    my_data = {
        'sql': 'update table set inventory=inventory' + num+'where product_id='+id
    }
    response = requests.post(IP, data=my_data)
    txt_result.delete('0.0', 'end')
    print(response.text)
    txt_result.insert('end', response.text)


window = tk.Tk()
window.title('seckill_buy')
window.geometry('800x800')
tk.Label(window, text='这是一个分布式秒杀系统', bg='grey', fg='pink', font=('仿宋', 22),
         width=30, height=2).pack()
tk.Label(window, text='商品名：', font=('仿宋', 20)).place(x=10, y=120)
tk.Label(window, text='商品ID：', font=('仿宋', 20)).place(x=10, y=180)
tk.Label(window, text='数量：', font=('仿宋', 20)).place(x=10, y=240)
Region1 = tk.Entry(window, show=None, font=('Consolas', 20))
Region2 = tk.Entry(window, show=None, font=('Consolas', 20))
Region3 = tk.Entry(window, show=None, font=('Consolas', 20))
Region1.place(x=110, y=120)
Region2.place(x=110, y=180)
Region3.place(x=110, y=240)
tk.Button(window, text='抢！！！', font=('仿宋', 24), command=func_buy).place(x=50, y=300)

tk.Label(window, text='商品ID：', font=('仿宋', 20)).place(x=420, y=120)
Region4 = tk.Entry(window, show=None, font=('Consolas', 20))
Region4.place(x=520, y=120)
tk.Button(window, text='查询库存', font=('仿宋', 24), command=func_query).place(x=500, y=300)

tk.Label(window, text='商品ID：', font=('仿宋', 20)).place(x=830, y=120)
tk.Label(window, text='补货数量：', font=('仿宋', 20)).place(x=830, y=180)
Region5 = tk.Entry(window, show=None, font=('Consolas', 20))
Region6 = tk.Entry(window, show=None, font=('Consolas', 20))
Region5.place(x=980, y=120)
Region6.place(x=980, y=180)
tk.Button(window, text='补货', font=('仿宋', 24), command=func_add).place(x=1000, y=300)
txt_result = tk.Text(window)
txt_result.place(x=400, y=400)
window.mainloop()
