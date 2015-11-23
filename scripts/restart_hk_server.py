#!/usr/bin/python

import paramiko
import threading
import time
import sys

ip = '47.89.25.26'
user = 'root'
password = ''
war = sys.argv[1]
prod_war='expper.war'
home='/root/apps/expper'
releases=home+"/releases"

def execute_cmds(ip, user, passwd, cmd):
    try:
        ssh = paramiko.SSHClient()
        ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
        ssh.connect(ip,22,user,passwd,timeout=5)
        for m in cmd:
            print m
            stdin, stdout, stderr = ssh.exec_command(m)
#           stdin.write("Y")
            out = stdout.readlines()
            for o in out:
                print o,
        print '%s\tOK\n'%(ip)
        ssh.close()
    except :
        print '%s\tError\n'%(ip)


if __name__=='__main__':
    print 'Start deploying %s to server %s'%(war, ip)

    cmd = [
        'service expper stop',
        'cp ' + releases + '/' + war +' ' + home + '/' + prod_war,
        'sleep 5 && service expper start'
    ]

    a=threading.Thread(target=execute_cmds, args=(ip,user,password,cmd))
    a.start()
