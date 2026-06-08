#!/usr/bin/env python3
"""Task App – Flask REST API with SQLite storage."""

import sqlite3
from datetime import datetime
from pathlib import Path
from flask import Flask, request, jsonify
from flask_cors import CORS

app = Flask(__name__)
CORS(app)

DB_PATH = Path(__file__).parent / "tasks.db"


def get_db():
    conn = sqlite3.connect(DB_PATH)
    conn.row_factory = sqlite3.Row
    return conn


def init_db():
    with get_db() as conn:
        conn.execute("""
            CREATE TABLE IF NOT EXISTS tasks (
                id          INTEGER PRIMARY KEY AUTOINCREMENT,
                title       TEXT    NOT NULL,
                description TEXT    DEFAULT '',
                status      TEXT    DEFAULT 'todo',
                priority    TEXT    DEFAULT 'medium',
                category    TEXT    DEFAULT 'general',
                due_date    TEXT,
                created_at  TEXT    NOT NULL,
                updated_at  TEXT    NOT NULL
            )
        """)


# ---------------------------------------------------------------------------
# Tasks CRUD
# ---------------------------------------------------------------------------

@app.route("/api/tasks", methods=["GET"])
def list_tasks():
    status   = request.args.get("status")
    priority = request.args.get("priority")
    category = request.args.get("category")
    search   = request.args.get("q")

    query  = "SELECT * FROM tasks WHERE 1=1"
    params = []

    if status:
        query += " AND status = ?"
        params.append(status)
    if priority:
        query += " AND priority = ?"
        params.append(priority)
    if category:
        query += " AND category = ?"
        params.append(category)
    if search:
        query += " AND (title LIKE ? OR description LIKE ?)"
        params += [f"%{search}%", f"%{search}%"]

    query += " ORDER BY CASE status WHEN 'todo' THEN 0 WHEN 'in-progress' THEN 1 ELSE 2 END, created_at DESC"

    with get_db() as conn:
        rows = conn.execute(query, params).fetchall()
        return jsonify([dict(r) for r in rows])


@app.route("/api/tasks", methods=["POST"])
def create_task():
    data = request.get_json(force=True)
    now  = datetime.utcnow().isoformat()
    with get_db() as conn:
        cur = conn.execute(
            """INSERT INTO tasks
                   (title, description, status, priority, category, due_date, created_at, updated_at)
               VALUES (?, ?, ?, ?, ?, ?, ?, ?)""",
            (
                data["title"],
                data.get("description", ""),
                data.get("status", "todo"),
                data.get("priority", "medium"),
                data.get("category", "general"),
                data.get("due_date"),
                now, now,
            ),
        )
        row = conn.execute("SELECT * FROM tasks WHERE id = ?", (cur.lastrowid,)).fetchone()
        return jsonify(dict(row)), 201


@app.route("/api/tasks/<int:task_id>", methods=["GET"])
def get_task(task_id):
    with get_db() as conn:
        row = conn.execute("SELECT * FROM tasks WHERE id = ?", (task_id,)).fetchone()
        if not row:
            return jsonify({"error": "Not found"}), 404
        return jsonify(dict(row))


@app.route("/api/tasks/<int:task_id>", methods=["PUT"])
def update_task(task_id):
    data = request.get_json(force=True)
    now  = datetime.utcnow().isoformat()
    with get_db() as conn:
        row = conn.execute("SELECT * FROM tasks WHERE id = ?", (task_id,)).fetchone()
        if not row:
            return jsonify({"error": "Not found"}), 404
        conn.execute(
            """UPDATE tasks
               SET title=?, description=?, status=?, priority=?, category=?, due_date=?, updated_at=?
               WHERE id=?""",
            (
                data.get("title",       row["title"]),
                data.get("description", row["description"]),
                data.get("status",      row["status"]),
                data.get("priority",    row["priority"]),
                data.get("category",    row["category"]),
                data.get("due_date",    row["due_date"]),
                now, task_id,
            ),
        )
        updated = conn.execute("SELECT * FROM tasks WHERE id = ?", (task_id,)).fetchone()
        return jsonify(dict(updated))


@app.route("/api/tasks/<int:task_id>", methods=["DELETE"])
def delete_task(task_id):
    with get_db() as conn:
        row = conn.execute("SELECT id FROM tasks WHERE id = ?", (task_id,)).fetchone()
        if not row:
            return jsonify({"error": "Not found"}), 404
        conn.execute("DELETE FROM tasks WHERE id = ?", (task_id,))
        return jsonify({"message": "deleted"})


# ---------------------------------------------------------------------------
# Stats
# ---------------------------------------------------------------------------

@app.route("/api/stats", methods=["GET"])
def stats():
    with get_db() as conn:
        def count(where=""):
            q = "SELECT COUNT(*) FROM tasks"
            if where:
                q += f" WHERE {where}"
            return conn.execute(q).fetchone()[0]
        return jsonify({
            "total":       count(),
            "todo":        count("status='todo'"),
            "in_progress": count("status='in-progress'"),
            "done":        count("status='done'"),
        })


if __name__ == "__main__":
    init_db()
    app.run(debug=True, port=5001)
