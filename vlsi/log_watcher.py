
import sys
import time
import subprocess

import requests
import os


mkey = os.environ["SENDER_KEY"]

def send_simple_message(email_addr, subject, body, dryRun=True):
    if dryRun:
        print("-------------------------------------------")
        print("DRY RUN ENABLED. WOULD HAVE SENT:")
        print(email_addr)
        print("-------------------------------------------")
        return

    resp = requests.post(
        "https://api.mailgun.net/v3/mg.sagark.org/messages",
        auth=("api", mkey),
        data={"from": "Tapeout Notifications <mailgun@mg.sagark.org>",
              "to": [email_addr],
              "subject": subject,
              "text": body})


    print(resp)
    return resp

def send_wrapper(subject, body):
    send_simple_message("karandikarsagar@gmail.com", subject, body, dryRun=False)



# how often to check the log for updates
check_interval_s = 300
# number of lines to check
tail_lines = 20

# argument to this file when called should be logfile to track
filename = sys.argv[1]

# heartbeat interval
# every heartbeat_interval_s, send an email even if things have
# been succeeding
#
# if you get no email at all in the last heartbeat_interval_s,
# something has gone wrong
heartbeat_interval_s = 1800

tracker_filename = "TEMP_LOG_TRACKER." + filename


time_elapsed = 0


last_check_lines = []

log_line_prefix = "WARNING-logline "

time_since_last_diff = 0

def pstring_time(time_s):
    mins = int(time_s / 60)
    secs = time_s % 60
    return f"{mins}m{secs}s"

def current_time_string():
    return time.strftime("%H:%M:%S on %Y-%m-%d")

last_sent_noprogress_notif = False
last_notif_time_elapsed = 0

while True:
    subprocess.call("tail -n " + str(tail_lines) + " " + filename + " > " + tracker_filename, shell=True)

    with open(tracker_filename, 'r') as logfile:
        this_round_lines = logfile.readlines()

        different = False
        if len(last_check_lines) != len(this_round_lines):
            different = True

        for l1, l2 in zip(this_round_lines, last_check_lines):
            if l1 != l2:
                different = True

        last_check_lines = this_round_lines

        if not different:
            last_sent_noprogress_notif = True
            time_since_last_diff += check_interval_s

            log_line_prefix_now = log_line_prefix + f"[{current_time_string()}]:"
            print("-----------------------------------------")
            failure_body = log_line_prefix_now + log_line_prefix_now.join(last_check_lines)
            print(failure_body)
            print("-----------------------------------------")

            failure_subject = f"WARNING [{current_time_string()}]: In the last {pstring_time(time_since_last_diff)}, the last {tail_lines} lines of the log have not changed (see above)."
            print(failure_subject)
            send_wrapper(failure_subject, failure_body)
            last_notif_time_elapsed = time_elapsed
        else:
            time_since_last_diff = 0
            print(f"INFO [{current_time_string()}]: Log has advanced. Total time: {pstring_time(time_elapsed)}.")
            if last_sent_noprogress_notif:
                last_sent_noprogress_notif = False

                success_subject = f"SUCCESS [{current_time_string()}]: Log has returned to advancing. Total time: {pstring_time(time_elapsed)}."
                print(success_subject)
                send_wrapper(success_subject, success_subject)
                last_notif_time_elapsed = time_elapsed
            elif (time_elapsed - last_notif_time_elapsed) > heartbeat_interval_s:
                success_subject = f"SUCCESS [{current_time_string()}]: Heartbeat update. Everything is progressing fine. Total time: {pstring_time(time_elapsed)}."
                print(success_subject)
                send_wrapper(success_subject, success_subject)
                last_notif_time_elapsed = time_elapsed


    print(f"INFO [{current_time_string()}]: Sleeping for {pstring_time(check_interval_s)}")
    time.sleep(check_interval_s)
    time_elapsed += check_interval_s

